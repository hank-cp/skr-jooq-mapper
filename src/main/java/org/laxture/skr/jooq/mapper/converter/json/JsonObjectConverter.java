package org.laxture.skr.jooq.mapper.converter.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.jooq.JSON;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.misc.MapperConversionException;
import org.laxture.skr.jooq.mapper.misc.RefectionUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

public class JsonObjectConverter implements SkrJooqConverter<Object, JSON> {

    protected final ObjectMapper objectMapper;

    public JsonObjectConverter(@NonNull ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public int match(Type modelType, Type jooqType) {
        if (!RefectionUtils.isAssignable(Collection.class, RefectionUtils.toClass(modelType))
            && !RefectionUtils.isAssignable(Map.class, RefectionUtils.toClass(modelType))
            && RefectionUtils.isAssignable(JSON.class, jooqType)) {
            return 11;
        }
        return MISMATCH;
    }

    @Override
    public JSON convertToJooqType(@NonNull Object mVal, Class<?> jooqType) {
        try {
            return JSON.valueOf(objectMapper.writeValueAsString(mVal));
        } catch (IOException e) {
            throw new MapperConversionException(getModelType(), getJooqType(), e);
        }
    }

    @Override
    public Object convertToModelType(@NonNull JSON jVal, Type modelType) {
        if ("null".equals(jVal.toString())) return null;
        try {
            return objectMapper.readValue(jVal.data(), RefectionUtils.toClass(modelType));
        } catch (IllegalArgumentException | JsonProcessingException e) {
            throw new MapperConversionException(getJooqType(), getModelType(), e);
        }
    }
}
