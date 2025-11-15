package org.laxture.skr.jooq.mapper.converter;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jooq.Record;
import org.laxture.skr.jooq.mapper.annotation.LeftoverCollector;
import org.laxture.skr.jooq.mapper.annotation.MappingInstantiator;
import org.laxture.skr.jooq.mapper.annotation.PrimaryKey;
import org.laxture.skr.jooq.mapper.annotation.Transient;
import org.laxture.skr.jooq.converter.number.*;
import org.laxture.skr.jooq.mapper.converter.json.JsonObjectConverter;
import org.laxture.skr.jooq.mapper.converter.number.Double2BigDecimalConverter;
import org.laxture.skr.jooq.mapper.converter.number.Long2BigIntegerConverter;
import org.laxture.skr.jooq.mapper.hook.MappingHook;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class ObjectConverter implements SkrJooqConverter<Object, Record> {

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

    private final List<SkrJooqConverter<?, ?>> customConverters;

    public ObjectConverter() {
        this(Collections.emptyList());
    }

    public ObjectConverter(List<SkrJooqConverter<?, ?>> customConverters) {
        this.customConverters = customConverters;
    }

//    @Override
//    public int match(Object mVal, Object jVal) {
//        if (jVal instanceof Record) {
//            return 0;
//        }
//        return MISMATCH;
//    }

    @Override
    public Record convertToJooqType(Object mVal) {
        throw new UnsupportedOperationException("Converting model object to Record is not supported in GenericObjectConverter");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object convertToModelType(Record jVal) {
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T convertToModelType(Record record, Class<T> modelClass) {
        if (record == null) return null;

        try {
            T instance = createInstance(modelClass);
            Map<String, Object> leftoverMap = new HashMap<>();
            Set<String> processedFields = new HashSet<>();

            mapFields(instance, record, "", processedFields, leftoverMap, modelClass);

            handleLeftoverCollector(instance, leftoverMap, modelClass);

            if (instance instanceof MappingHook) {
                ((MappingHook) instance).postMapping();
            }

            return instance;

        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Record to " + modelClass.getName(), e);
        }
    }

    private <T> T createInstance(Class<T> clazz) throws Exception {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(MappingInstantiator.class)) {
                constructor.setAccessible(true);
                Object[] params = new Object[constructor.getParameterCount()];
                return (T) constructor.newInstance(params);
            }
        }

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(MappingInstantiator.class) &&
                java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                method.setAccessible(true);
                return (T) method.invoke(null);
            }
        }

        return clazz.getDeclaredConstructor().newInstance();
    }

    private void mapFields(Object instance, Record record, String prefix,
                          Set<String> processedFields, Map<String, Object> leftoverMap,
                          Class<?> modelClass) throws Exception {

        for (Field field : getAllFields(modelClass)) {
            if (shouldSkipField(field)) continue;

            field.setAccessible(true);
            String fieldName = getFieldName(field);
            String fullFieldName = prefix.isEmpty() ? fieldName : prefix + SEPARATOR + fieldName;

            if (isNestedObject(field.getType())) {
                Object nestedObject = mapNestedObject(record, field.getType(), fullFieldName, processedFields);

                if (shouldSetNestedObjectToNull(nestedObject, field)) {
                    field.set(instance, null);
                } else {
                    field.set(instance, nestedObject);
                }
            } else {
                Object value = getValueFromRecord(record, fullFieldName);
                if (value != null) {
                    processedFields.add(fullFieldName);
                    Object convertedValue = convertValue(value, field.getType());
                    field.set(instance, convertedValue);
                }
            }
        }

        for (int i = 0; i < record.size(); i++) {
            String recordFieldName = record.field(i).getName();
            if (!processedFields.contains(recordFieldName)) {
                leftoverMap.put(recordFieldName, record.get(i));
            }
        }
    }

    private boolean shouldSkipField(Field field) {
        return field.isAnnotationPresent(Transient.class) ||
               field.isAnnotationPresent(LeftoverCollector.class);
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

    private Object mapNestedObject(Record record, Class<?> nestedType,
                                   String prefix, Set<String> processedFields) throws Exception {
        Object nestedInstance = createInstance(nestedType);
        Map<String, Object> nestedLeftover = new HashMap<>();

        mapFields(nestedInstance, record, prefix, processedFields, nestedLeftover, nestedType);

        handleLeftoverCollector(nestedInstance, nestedLeftover, nestedType);

        if (nestedInstance instanceof MappingHook) {
            ((MappingHook) nestedInstance).postMapping();
        }

        return nestedInstance;
    }

    private boolean shouldSetNestedObjectToNull(Object nestedObject, Field field) throws Exception {
        for (Field nestedField : getAllFields(nestedObject.getClass())) {
            if (nestedField.isAnnotationPresent(PrimaryKey.class)) {
                nestedField.setAccessible(true);
                if (nestedField.get(nestedObject) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    private Object getValueFromRecord(Record record, String fieldName) {
        try {
            return record.get(fieldName);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType.isInstance(value)) return value;

        List<SkrJooqConverter<?, ?>> allConverters = new ArrayList<>();
        allConverters.addAll(customConverters);
        allConverters.addAll(DEFAULT_CONVERTERS);

        SkrJooqConverter bestConverter = null;
        int bestPriority = Integer.MIN_VALUE;

//        for (SkrJooqConverter converter : allConverters) {
//            try {
//                int priority = converter.match(null, value);
//                if (priority >= 0 && priority > bestPriority) {
//                    bestConverter = converter;
//                    bestPriority = priority;
//                }
//            } catch (Exception ignored) {
//            }
//        }

        if (bestConverter != null) {
            try {
                Object converted = bestConverter.convertToModelType(value);
                if (targetType.isInstance(converted)) {
                    return converted;
                }
            } catch (Exception ignored) {
            }
        }

        return value;
    }

    private void handleLeftoverCollector(Object instance, Map<String, Object> leftoverMap,
                                        Class<?> modelClass) throws Exception {
        for (Field field : getAllFields(modelClass)) {
            if (field.isAnnotationPresent(LeftoverCollector.class)) {
                field.setAccessible(true);
                if (Map.class.isAssignableFrom(field.getType())) {
                    field.set(instance, leftoverMap);
                }
            }
        }
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
