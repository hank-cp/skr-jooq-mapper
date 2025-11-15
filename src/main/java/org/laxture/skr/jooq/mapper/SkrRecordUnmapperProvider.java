package org.laxture.skr.jooq.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jooq.Record;
import org.jooq.RecordType;
import org.jooq.RecordUnmapper;
import org.jooq.RecordUnmapperProvider;
import org.laxture.skr.jooq.mapper.annotation.Immutable;
import org.laxture.skr.jooq.mapper.annotation.Transient;
import org.laxture.skr.jooq.mapper.converter.json.JsonObjectConverter;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.converter.number.Double2BigDecimalConverter;
import org.laxture.skr.jooq.mapper.converter.number.Long2BigIntegerConverter;

import java.lang.reflect.Field;
import java.util.*;

public class SkrRecordUnmapperProvider implements RecordUnmapperProvider {

    private static final String SEPARATOR = "_";
    private static final List<SkrJooqConverter<?, ?>> DEFAULT_CONVERTERS = Arrays.asList(
            new StringConverter(),
            new IntegerConverter(),
            new Long2BigIntegerConverter(),
            new FloatConverter(),
            new Double2BigDecimalConverter(),
            new BooleanConverter(),
            new LocalDateConverter(),
            new LocalDateTimeConverter(),
            new LocalTimeConverter(),
            new JsonObjectConverter()
    );

    private final List<SkrJooqConverter<?, ?>> customConverters = new ArrayList<>();

    public SkrRecordUnmapperProvider() {
    }

    public void registerConverter(SkrJooqConverter<?, ?> converter) {
        customConverters.add(converter);
    }

    @Override
    public <E, R extends Record> RecordUnmapper<E, R> provide(Class<? extends E> type, RecordType<R> recordType) {
        return new SkrRecordUnmapper<>(customConverters);
    }

    private static class SkrRecordUnmapper<E, R extends Record> implements RecordUnmapper<E, R> {
        private final List<SkrJooqConverter<?, ?>> customConverters;

        public SkrRecordUnmapper(List<SkrJooqConverter<?, ?>> customConverters) {
            this.customConverters = customConverters;
        }

        @Override
        @SuppressWarnings("unchecked")
        public R unmap(E model) {
            if (model == null) return null;

            Map<String, Object> fieldValues = new HashMap<>();
            unmapObject(model, "", fieldValues, false);

            return (R) createRecord(fieldValues);
        }

        private Record createRecord(Map<String, Object> fieldValues) {
            return null;
        }

        private void unmapObject(Object obj, String prefix, Map<String, Object> values, boolean isUpdate) {
            if (obj == null) return;

            for (Field field : getAllFields(obj.getClass())) {
                if (shouldSkipField(field, isUpdate)) continue;

                try {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    if (value == null) continue;

                    String fieldName = getFieldName(field);
                    String fullFieldName = prefix.isEmpty() ? fieldName : prefix + SEPARATOR + fieldName;

                    if (isNestedObject(field.getType())) {
                        unmapObject(value, fullFieldName, values, isUpdate);
                    } else {
                        Object convertedValue = convertToJooqType(value);
                        values.put(fullFieldName, convertedValue);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to unmap field: " + field.getName(), e);
                }
            }
        }

        private boolean shouldSkipField(Field field, boolean isUpdate) {
            if (field.isAnnotationPresent(Transient.class)) {
                return true;
            }
            if (isUpdate && field.isAnnotationPresent(Immutable.class)) {
                return true;
            }
            return false;
        }

        private String getFieldName(Field field) {
            JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
            if (jsonProperty != null && !jsonProperty.value().isEmpty()) {
                return jsonProperty.value();
            }
            return field.getName();
        }

        private boolean isNestedObject(Class<?> type) {
            return !type.isPrimitive() &&
                   !type.getName().startsWith("java.lang") &&
                   !type.getName().startsWith("java.time") &&
                   !type.getName().startsWith("java.util") &&
                   !type.isArray() &&
                   !type.isEnum();
        }

        @SuppressWarnings("unchecked")
        private Object convertToJooqType(Object value) {
            if (value == null) return null;

            List<SkrJooqConverter<?, ?>> allConverters = new ArrayList<>();
            allConverters.addAll(customConverters);
            allConverters.addAll(DEFAULT_CONVERTERS);

            SkrJooqConverter bestConverter = null;
            int bestPriority = Integer.MIN_VALUE;

            for (SkrJooqConverter converter : allConverters) {
                try {
                    int priority = converter.match(value.getClass(), value.getClass());
                    if (priority >= 0 && priority > bestPriority) {
                        bestConverter = converter;
                        bestPriority = priority;
                    }
                } catch (Exception ignored) {
                }
            }

            if (bestConverter != null) {
                try {
                    return bestConverter.convertToJooqType(value);
                } catch (Exception ignored) {
                }
            }

            return value;
        }

        private List<Field> getAllFields(Class<?> clazz) {
            List<Field> fields = new ArrayList<>();
            Class<?> current = clazz;
            while (current != null && current != Object.class) {
                fields.addAll(Arrays.asList(current.getDeclaredFields()));
                current = current.getSuperclass();
            }
            return fields;
        }
    }
}
