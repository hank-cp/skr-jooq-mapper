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
package org.laxture.skr.jooq.mapper.converter.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.NonNull;
import org.jooq.JSONB;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.misc.MapperConversionException;
import org.laxture.skr.jooq.mapper.misc.RefectionUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Converter for type conversion operations.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class JsonbArrayConverter implements SkrJooqConverter<List<?>, JSONB> {

    private final ObjectMapper objectMapper;

    public JsonbArrayConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public int match(@NonNull Type modelType, @NonNull Type jooqType) {
        if (RefectionUtils.isAssignable(List.class, modelType)
            && RefectionUtils.isAssignable(JSONB.class, jooqType)) {
            return 12;
        }
        return MISMATCH;
    }

    @Override
    public JSONB convertToJooqType(List<?> mVal, Class<?> jooqType) {
        try {
            return JSONB.valueOf(objectMapper.writeValueAsString(mVal));
        } catch (IOException e) {
            throw new MapperConversionException(getModelType(), getJooqType(), e);
        }
    }

    @Override
    public List<?> convertToModelType(JSONB jVal, Type modelType) {
        if ("null".equals(jVal.toString())) return null;

        Type elementType = RefectionUtils.getComponentTypeOfListOrArray(modelType);
        if (elementType == null) {
            throw new MapperConversionException(getJooqType(), getModelType());
        }
        Class<?> elementClass = RefectionUtils.toClass(elementType);
        CollectionType collectionType = objectMapper.getSerializationConfig()
            .getTypeFactory().constructCollectionType(List.class, elementClass);
        try {
            return objectMapper.readValue(jVal.data(), collectionType);
        } catch (IllegalArgumentException | JsonProcessingException e) {
            throw new MapperConversionException(getJooqType(), getModelType(), e);
        }
    }
}
