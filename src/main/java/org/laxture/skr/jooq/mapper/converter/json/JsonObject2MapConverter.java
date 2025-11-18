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
import com.fasterxml.jackson.databind.type.MapType;
import lombok.NonNull;
import org.jooq.JSON;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.misc.MapperConversionException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Converter for converting JsonObject to Map.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class JsonObject2MapConverter implements SkrJooqConverter<Map<String, Object>, JSON> {

    protected final ObjectMapper objectMapper;

    public JsonObject2MapConverter(@NonNull ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public JSON convertToJooqType(@NonNull Map<String, Object> mVal, Class<?> jooqType) {
        try {
            return JSON.valueOf(objectMapper.writeValueAsString(mVal));
        } catch (IOException e) {
            throw new MapperConversionException(getModelType(), getJooqType(), e);
        }
    }

    @Override
    public Map<String, Object> convertToModelType(@NonNull JSON jVal, Type modelType) {
        if ("null".equals(jVal.toString())) return null;
        try {
            MapType mapType = objectMapper.getSerializationConfig()
                .getTypeFactory().constructMapType(Map.class, String.class, Object.class);
            return objectMapper.readValue(jVal.data(), mapType);
        } catch (IllegalArgumentException | JsonProcessingException e) {
            throw new MapperConversionException(getJooqType(), getModelType(), e);
        }
    }
}
