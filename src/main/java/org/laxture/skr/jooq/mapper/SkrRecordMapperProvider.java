package org.laxture.skr.jooq.mapper;

import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.jooq.Record;
import org.laxture.skr.jooq.mapper.annotation.LeftoverCollector;
import org.laxture.skr.jooq.mapper.converter.ConverterRegistry;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.hook.MappingHook;
import org.laxture.skr.jooq.mapper.misc.RefectionUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class SkrRecordMapperProvider implements RecordMapperProvider {

    private final ConverterRegistry converterRegistry;
    private final TableFieldCaseType tableFieldCaseType;

    public SkrRecordMapperProvider(ConverterRegistry converterRegistry,
                                   TableFieldCaseType tableFieldCaseType) {
        this.converterRegistry = converterRegistry;
        this.tableFieldCaseType = tableFieldCaseType;
    }

    @Override
    public <R extends Record, E> RecordMapper<R, E> provide(RecordType<R> recordType, Class<? extends E> type) {
        return new SkrRecordMapper<>(type);
    }

    private class SkrRecordMapper<R extends Record, E> implements RecordMapper<R, E> {
        private final Class<? extends E> modelType;

        public SkrRecordMapper(Class<? extends E> targetType) {
            this.modelType = targetType;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E map(R record) {
            if (record == null) return null;

            if (record.size() == 1) {
                Object value = record.get(0);
                Object converted = convertFieldValue(value, modelType);
                if (modelType.isInstance(converted)) {
                    return (E) converted;
                }
            }

            E modelInstance = RefectionUtils.createInstance(modelType);
            Set<String> processedFields = new HashSet<>();
            Map<RefectionUtils.FieldTuple, Boolean> nestedObjectFieldTouchFlags = new HashMap<>();

            for (Field<?> recordField : record.fields()) {
                Object jVal = recordField.get(record);
                String fieldName = convertToCamelCase(recordField.getName());

                // find model field, convert and set value
                RefectionUtils.FieldTuple modelField = RefectionUtils.findMatchModelField(modelInstance, fieldName);
                if (modelField != null) {
                    Object converted = convertFieldValue(jVal, modelField.getField().getType());
                    if (converted == null) continue;
                    RefectionUtils.setFieldValue(
                        modelField.getOwner(), modelField.getField(), converted);
                    processedFields.add(recordField.getName());
                    modelField.settle();
                }
            }
            handleLeftoverCollector(modelInstance, record, processedFields, modelType);

            if (modelInstance instanceof MappingHook hook) {
                hook.postMapping();
            }
            return modelInstance;
        }
    }

    @SuppressWarnings("unchecked")
    private <ModelType, JooqType> ModelType convertFieldValue(JooqType jVal, Class<?> modelType) {
        if (jVal == null) return null;

        SkrJooqConverter<ModelType, JooqType> converter =
            (SkrJooqConverter<ModelType, JooqType>) converterRegistry.matchConverter(modelType, jVal.getClass());

        if (converter == null) {
            log.warn("No converter found for model type {} and jooq type {}", modelType, jVal.getClass());
            return null;
        }

        return converter.convertToModelType(jVal);
    }

    private String convertToCamelCase(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) return fieldName;

        switch (tableFieldCaseType) {
            case CAMEL_CASE:
                return fieldName;
            case SNAKE_CASE:
            case SCREAMING_SNAKE_CASE:
                String[] parts = fieldName.split("_");
                StringBuilder result = new StringBuilder(parts[0].toLowerCase());
                for (int i = 1; i < parts.length; i++) {
                    if (!parts[i].isEmpty()) {
                        result.append(Character.toUpperCase(parts[i].charAt(0)))
                              .append(parts[i].substring(1).toLowerCase());
                    }
                }
                return result.toString();
            case KEBAB_CASE:
                String[] kebabParts = fieldName.split("-");
                StringBuilder kebabResult = new StringBuilder(kebabParts[0].toLowerCase());
                for (int i = 1; i < kebabParts.length; i++) {
                    if (!kebabParts[i].isEmpty()) {
                        kebabResult.append(Character.toUpperCase(kebabParts[i].charAt(0)))
                                   .append(kebabParts[i].substring(1).toLowerCase());
                    }
                }
                return kebabResult.toString();
            default:
                return fieldName;
        }
    }

    private void handleLeftoverCollector(Object instance,
                                         Record record, Set<String> processedFields,
                                         Class<?> modelType) {
        java.lang.reflect.Field leftoverField = RefectionUtils.findFieldAnnotatedWith(modelType, LeftoverCollector.class);

        // collect leftover fields
        Map<String, Object> leftoverMap = new HashMap<>();
        for (Field<?> field : record.fields()) {
            String fieldName = field.getName();
            if (processedFields.contains(fieldName)) continue;
            leftoverMap.put(fieldName, field.get(record));
        }

        // set leftover field to the model instance
        if (leftoverMap.isEmpty()
            || leftoverField == null
            || !Map.class.isAssignableFrom(leftoverField.getType())) return;
        RefectionUtils.setFieldValue(instance, leftoverField, leftoverMap);
    }
}
