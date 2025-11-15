package org.laxture.skr.jooq.mapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.*;
import org.jooq.Record;
import org.laxture.skr.jooq.mapper.annotation.LeftoverCollector;
import org.laxture.skr.jooq.mapper.annotation.MappingInstantiator;
import org.laxture.skr.jooq.mapper.converter.ConverterRegistry;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.hook.MappingHook;
import org.laxture.skr.jooq.mapper.misc.TypeHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

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
        private final Class<? extends E> targetType;

        public SkrRecordMapper(Class<? extends E> targetType) {
            this.targetType = targetType;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E map(R record) {
            if (record == null) return null;

            if (record.size() == 1) {
                Object value = record.get(0);
                Object converted = convertFieldValue(value, targetType);
                if (targetType.isInstance(converted)) {
                    return (E) converted;
                }
            }

            E modelInstance = createInstance(targetType);
            Set<String> processedFields = new HashSet<>();

            for (Field<?> field : record.fields()) {
                Object jVal = field.get(record);

                String fieldName = field.getName();
                if (processedFields.contains(fieldName)) continue;
                processedFields.add(fieldName);

                // find model field, convert and set value
                TypeHelper.FieldTuple matchedField = findMatchModelField(targetType, fieldName);
                if (matchedField == null) {
                    Object converted = convertFieldValue(jVal, matchedField.getField().getType());
                    if (converted == null) continue;
                    matchedField.getField().setAccessible(true);
                    try {
                        matchedField.getField().set(matchedField.getOwner(), converted);
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Field " + matchedField.getField() + " is not accessible");
                    }
                }
            }
            handleLeftoverCollector(modelInstance, record, processedFields, targetType);

            if (modelInstance instanceof MappingHook hook) {
                hook.postMapping();
            }
            return modelInstance;
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
    }

    private <T> T createInstance(Class<T> clazz) {
        Supplier<T> instructor = null;

        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterCount() != 0) continue;
            instructor = () -> {
                constructor.setAccessible(true);
                try {
                    return (T) constructor.newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException("Constructor " + constructor + " is not accessible");
                }
            };
            if (constructor.isAnnotationPresent(MappingInstantiator.class)) {
                return instructor.get();
            }
        }

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(MappingInstantiator.class)
                && Modifier.isStatic(method.getModifiers())
                && clazz.isAssignableFrom(method.getReturnType())) {
                method.setAccessible(true);
                try {
                    return (T) method.invoke(null);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException("Builder " + method + " is not accessible");
                }
            }
        }

        if (instructor != null) {
            return instructor.get();
        }

        throw new IllegalStateException("No constructor found for " + clazz);
    }

    private TypeHelper.FieldTuple findMatchModelField(Class<?> ModelClass, String fieldName) {
        // 1. turn fieldName to camel case according to tableFieldCaseType
        // 2. find model field. If not existed, check if it is nested object field.
        // 3. if yes, construct nested object and continue to match field recursively.
    }

    private void handleLeftoverCollector(Object instance,
                                         Record record, Set<String> processedFields,
                                         Class<?> modelType) {
        java.lang.reflect.Field leftoverField = TypeHelper.findFieldAnnotatedWith(modelType, LeftoverCollector.class);

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
        leftoverField.setAccessible(true);
        try {
            leftoverField.set(instance, leftoverMap);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Leftover collector field " + leftoverField + " is not accessible");
        }
    }
}
