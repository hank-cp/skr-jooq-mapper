package org.laxture.skr.jooq.mapper.converter.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.NonNull;
import org.jooq.JSON;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.misc.MapperConversionException;
import org.laxture.skr.jooq.mapper.misc.TypeHelper;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class JsonArrayConverter<ModelType> implements SkrJooqConverter<List<ModelType>, JSON> {

    private final ObjectMapper objectMapper;

    public JsonArrayConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public int match(@NonNull Type modelType, @NonNull Type jooqType) {
        if (modelType instanceof ParameterizedType
            && List.class.isAssignableFrom((Class<?>) ((ParameterizedType) modelType).getRawType())
            && TypeHelper.areEquals(TypeHelper.getComponentTypeOfListOrArray(modelType), modelType)
            && TypeHelper.isAssignable(JSON.class, jooqType)) {
            return 9;
        }
        return MISMATCH;
    }

    @Override
    public JSON convertToJooqType(List<ModelType> mVal) {
        try {
            return JSON.valueOf(objectMapper.writeValueAsString(mVal));
        } catch (IOException e) {
            throw new MapperConversionException(getModelType(), getJooqType(), e);
        }
    }

    @Override
    public List<ModelType> convertToModelType(JSON jVal) {
        if ("null".equals(jVal.toString())) return null;

        ParameterizedType modelType = (ParameterizedType) getModelType();
        Class<?> elementType = (Class<?>) modelType.getActualTypeArguments()[0];
        CollectionType collectionType = objectMapper.getSerializationConfig()
            .getTypeFactory().constructCollectionType(List.class, elementType);
        try {
            return objectMapper.convertValue(jVal, collectionType);
        } catch (IllegalArgumentException e) {
            throw new MapperConversionException(getJooqType(), getModelType(), e);
        }
    }
}
