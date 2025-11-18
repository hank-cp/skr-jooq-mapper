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
