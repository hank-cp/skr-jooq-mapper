package org.laxture.skr.jooq.mapper.converter.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.NonNull;
import org.jooq.JSON;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.misc.MapperConversionException;
import org.laxture.skr.jooq.mapper.misc.RefectionUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class JsonArrayConverter implements SkrJooqConverter<List<?>, JSON> {

    private final ObjectMapper objectMapper;

    public JsonArrayConverter(@NonNull ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public int match(@NonNull Type modelType, @NonNull Type jooqType) {
        if (RefectionUtils.isAssignable(List.class, modelType)
            && RefectionUtils.isAssignable(JSON.class, jooqType)) {
            return 12;
        }
        return MISMATCH;
    }

    @Override
    public JSON convertToJooqType(List<?> mVal, Class<?> jooqType) {
        try {
            return JSON.valueOf(objectMapper.writeValueAsString(mVal));
        } catch (IOException e) {
            throw new MapperConversionException(getModelType(), getJooqType(), e);
        }
    }

    @Override
    public List<?> convertToModelType(JSON jVal, Type modelType) {
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
