package org.laxture.skr.jooq.mapper;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.jooq.Record;
import org.laxture.skr.jooq.mapper.annotation.LeftoverCollector;
import org.laxture.skr.jooq.mapper.converter.ConverterRegistry;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.misc.RefectionUtils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class SkrRecordUnmapperProvider implements RecordUnmapperProvider {

    private final DSLContextProvider dslContextProvider;
    private final ConverterRegistry converterRegistry;
    private final TableFieldCaseType tableFieldCaseType;

    private final List<SkrJooqConverter<?, ?>> customConverters = new ArrayList<>();

    public SkrRecordUnmapperProvider(DSLContextProvider dslContextProvider,
                                     ConverterRegistry converterRegistry,
                                     TableFieldCaseType tableFieldCaseType) {
        this.dslContextProvider = dslContextProvider;
        this.converterRegistry = converterRegistry;
        this.tableFieldCaseType = tableFieldCaseType;
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
                String recordFieldName = recordField.getName();
                String camelFieldName = convertToCamelCase(recordFieldName);

                Object modelValue = findModelValue(model, camelFieldName);

                if (modelValue == null) {
                    modelValue = findValueFromLeftoverCollector(model, recordFieldName);
                }

                if (modelValue != null) {
                    Object jooqValue = convertFieldValue(modelValue, recordField.getType());
                    record.set((Field<Object>) recordField, jooqValue);
                }
            }

            return record;
        }

        private Object findModelValue(Object modelInstance, String fieldName) {
            if (modelInstance == null) return null;

            String camelFieldName = convertToCamelCase(fieldName);
            java.lang.reflect.Field field = RefectionUtils.findField(modelInstance.getClass(), camelFieldName);

            if (field != null && !isTransientField(field)) {
                field.setAccessible(true);
                try {
                    return field.get(modelInstance);
                } catch (IllegalAccessException e) {
                    log.warn("Cannot access field {} in class {}", field.getName(), modelInstance.getClass());
                    return null;
                }
            }

            if (fieldName.contains("_")) {
                int separatorIndex = fieldName.indexOf("_");
                String nestedFieldName = fieldName.substring(0, separatorIndex);
                String remainingFieldName = fieldName.substring(separatorIndex + 1);

                String camelNestedFieldName = convertToCamelCase(nestedFieldName);
                java.lang.reflect.Field nestedField = RefectionUtils.findField(modelInstance.getClass(), camelNestedFieldName);

                if (nestedField != null && !isTransientField(nestedField)) {
                    nestedField.setAccessible(true);
                    try {
                        Object nestedObject = nestedField.get(modelInstance);
                        if (nestedObject != null) {
                            return findModelValue(nestedObject, remainingFieldName);
                        }
                    } catch (IllegalAccessException e) {
                        log.warn("Cannot access nested field {} in class {}", nestedField.getName(), modelInstance.getClass());
                    }
                }
            }

            return null;
        }

        private Object findValueFromLeftoverCollector(Object modelInstance, String fieldName) {
            java.lang.reflect.Field leftoverField = RefectionUtils.findFieldAnnotatedWith(
                modelInstance.getClass(), LeftoverCollector.class);

            if (leftoverField == null || !Map.class.isAssignableFrom(leftoverField.getType())) {
                return null;
            }

            leftoverField.setAccessible(true);
            try {
                Map<String, Object> leftoverMap = (Map<String, Object>) leftoverField.get(modelInstance);
                if (leftoverMap != null) {
                    return leftoverMap.get(fieldName);
                }
            } catch (IllegalAccessException e) {
                log.warn("Cannot access leftover collector field in class {}", modelInstance.getClass());
            }

            return null;
        }

        private boolean isTransientField(java.lang.reflect.Field field) {
            return field.isAnnotationPresent(org.laxture.skr.jooq.mapper.annotation.Transient.class)
                || field.isAnnotationPresent(org.springframework.data.annotation.Transient.class)
                || field.isAnnotationPresent(java.beans.Transient.class)
                || (field.getModifiers() & Modifier.TRANSIENT) != 0;
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

        return converter.convertToJooqType(mVal);
    }
}
