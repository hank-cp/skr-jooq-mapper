package org.laxture.skr.jooq.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.jooq.Record;
import org.laxture.skr.jooq.mapper.annotation.LeftoverCollector;
import org.laxture.skr.jooq.mapper.converter.ConverterRegistry;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.misc.NamingUtils;
import org.laxture.skr.jooq.mapper.misc.RefectionUtils;

import java.lang.reflect.Modifier;
import java.util.Map;

@Slf4j
public class SkrRecordUnmapperProvider implements RecordUnmapperProvider {

    private final DSLContextProvider dslContextProvider;
    private final ConverterRegistry converterRegistry;
    private final TableFieldCaseType tableFieldCaseType;
    private final ObjectMapper objectMapper;

    public SkrRecordUnmapperProvider(DSLContextProvider dslContextProvider,
                                     ConverterRegistry converterRegistry,
                                     TableFieldCaseType tableFieldCaseType,
                                     ObjectMapper objectMapper) {
        this.dslContextProvider = dslContextProvider;
        this.converterRegistry = converterRegistry;
        this.tableFieldCaseType = tableFieldCaseType;
        this.objectMapper = objectMapper;
    }

    @Override
    public <E, R extends Record> RecordUnmapper<E, R> provide(Class<? extends E> type, RecordType<R> recordType) {
        return new SkrRecordUnmapper<>(recordType);
    }

    private class SkrRecordUnmapper<E, R extends Record> implements RecordUnmapper<E, R> {
        private final RecordType<R> recordType;

        SkrRecordUnmapper(RecordType<R> recordType) {
            this.recordType = recordType;
        }

        @Override
        @SuppressWarnings("unchecked")
        public R unmap(@NonNull E model) {
            R record = (R) dslContextProvider.provide().newRecord(recordType.fields());

            for (Field<?> recordField : record.fields()) {
                String fieldName = NamingUtils.convertToCamelCase(
                    tableFieldCaseType, recordField.getName());

                Object mVal;
                RefectionUtils.FieldTuple modelField = RefectionUtils.findMatchModelField(model, fieldName);
                if (modelField != null) {
                    // find model field
                    if (isTransientField(modelField.getField())) continue;
                    mVal = RefectionUtils.getFieldValue(modelField.getOwner(), modelField.getField());
                } else {
                    // try to find value from leftover collector
                    mVal = findValueFromLeftoverCollector(model, fieldName);
                }

                if (mVal != null) {
                    Object jooqValue = convertFieldValue(mVal, recordField.getType());
                    record.set((Field<Object>) recordField, jooqValue);
                }
            }

            return record;
        }

        private Object findValueFromLeftoverCollector(Object modelInstance, String fieldName) {
            java.lang.reflect.Field leftoverField = RefectionUtils.findFieldAnnotatedWith(
                modelInstance.getClass(), LeftoverCollector.class);
            if (leftoverField == null || !Map.class.isAssignableFrom(leftoverField.getType())) return null;

            Map<String, Object> leftoverMap = RefectionUtils.getFieldValue(modelInstance, leftoverField);
            if (leftoverMap == null || leftoverMap.isEmpty()) return null;
            return leftoverMap.get(fieldName);
        }

        private boolean isTransientField(java.lang.reflect.Field field) {
            return field.isAnnotationPresent(org.laxture.skr.jooq.mapper.annotation.Transient.class)
                || field.isAnnotationPresent(org.springframework.data.annotation.Transient.class)
                || field.isAnnotationPresent(java.beans.Transient.class)
                || (field.getModifiers() & Modifier.TRANSIENT) != 0;
        }
    }

    @SuppressWarnings("unchecked")
    private <ModelType, JooqType> JooqType convertFieldValue(ModelType mVal, Class<?> jooqType) {
        if (mVal == null) return null;

        SkrJooqConverter<ModelType, JooqType> converter =
            (SkrJooqConverter<ModelType, JooqType>) converterRegistry.matchConverter(mVal.getClass(), jooqType);

        if (converter == null) {
            log.warn("No converter found for jooq type {} and model type {}", jooqType, mVal.getClass());
            return null;
        }

        return converter.convertToJooqType(mVal, jooqType);
    }
}
