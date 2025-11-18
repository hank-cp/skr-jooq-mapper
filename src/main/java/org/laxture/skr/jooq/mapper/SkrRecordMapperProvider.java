/*
 * Copyright (C) 2019-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.laxture.skr.jooq.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.jooq.Record;
import org.laxture.skr.jooq.mapper.annotation.LeftoverCollector;
import org.laxture.skr.jooq.mapper.converter.ConverterRegistry;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.hook.MappingHook;
import org.laxture.skr.jooq.mapper.misc.NamingUtils;
import org.laxture.skr.jooq.mapper.misc.RefectionUtils;

import java.util.*;

@Slf4j
/**
 * Provider for SkrRecordMapper instances.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class SkrRecordMapperProvider implements RecordMapperProvider {

    private final ConverterRegistry converterRegistry;
    private final TableFieldCaseType tableFieldCaseType;
    private final ObjectMapper objectMapper;

    public SkrRecordMapperProvider(ConverterRegistry converterRegistry,
                                   TableFieldCaseType tableFieldCaseType,
                                   ObjectMapper objectMapper) {
        this.converterRegistry = converterRegistry;
        this.tableFieldCaseType = tableFieldCaseType;
        this.objectMapper = objectMapper;
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

            for (Field<?> recordField : record.fields()) {
                Object jVal = recordField.get(record);
                String fieldName = NamingUtils.convertToCamelCase(
                    tableFieldCaseType, recordField.getName());

                // find model field, convert and set value
                RefectionUtils.FieldTuple modelField = RefectionUtils.findMatchModelField(modelInstance, fieldName);
                if (modelField != null) {
                    processedFields.add(recordField.getName()); // find matched model field, mark as processed, don't collect as leftover

                    Object converted = convertFieldValue(jVal, modelField.getField().getGenericType());
                    if (converted == null) continue;
                    RefectionUtils.setFieldValue(
                        modelField.getOwner(), modelField.getField(), converted);
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
    private <ModelType, JooqType> ModelType convertFieldValue(JooqType jVal, java.lang.reflect.Type modelType) {
        if (jVal == null) return null;

        SkrJooqConverter<ModelType, JooqType> converter =
            (SkrJooqConverter<ModelType, JooqType>) converterRegistry.matchConverter(modelType, jVal.getClass());

        if (converter == null) {
            log.warn("No converter found for model type {} and jooq type {}", modelType, jVal.getClass());
            return null;
        }

        return converter.convertToModelType(jVal, modelType);
    }

    private void handleLeftoverCollector(Object instance,
                                         Record record, Set<String> processedFields,
                                         Class<?> modelType) {
        java.lang.reflect.Field leftoverField = RefectionUtils.findFieldAnnotatedWith(
            modelType, LeftoverCollector.class);
        if (leftoverField == null || !Map.class.isAssignableFrom(leftoverField.getType())) return;

        // collect leftover fields
        Map<String, Object> leftoverMap = new HashMap<>();
        for (Field<?> field : record.fields()) {
            String fieldName = field.getName();
            if (processedFields.contains(fieldName) || field.get(record) == null) continue;
            Object converted;
            if (RefectionUtils.areEquals(JSON.class, field.getType())
                || RefectionUtils.areEquals(JSONB.class, field.getType())) {
                String jsonStr = field.get(record).toString();
                try {
                    if (jsonStr.startsWith("{")) {
                        converted = objectMapper.readValue(jsonStr, Map.class);
                    } else if (jsonStr.startsWith("[")) {
                        converted = objectMapper.readValue(jsonStr, List.class);
                    } else {
                        converted = jsonStr;
                    }
                } catch (JsonProcessingException e) {
                    log.warn("Failed to read JSON string {} to Map or List", jsonStr, e);
                    converted = jsonStr;
                }
            } else {
                converted = field.get(record);
            }
            if (converted != null) leftoverMap.put(
                NamingUtils.convertToCamelCase(tableFieldCaseType, fieldName), converted);
        }

        if (leftoverMap.isEmpty()) return;
        // set leftover field to the model instance
        RefectionUtils.setFieldValue(instance, leftoverField, leftoverMap);
    }
}
