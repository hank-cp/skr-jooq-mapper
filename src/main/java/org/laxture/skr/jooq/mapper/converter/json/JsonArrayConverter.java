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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class JsonArrayConverter<ModelType> implements SkrJooqConverter<List<ModelType>, JSON> {

    private final ObjectMapper objectMapper;
    private final Class<ModelType> _modelType;

    @SuppressWarnings("unchecked")
    public JsonArrayConverter(@NonNull ObjectMapper objectMapper,
                              @NonNull Class<ModelType> modelType) {
        this.objectMapper = objectMapper;
        this._modelType = modelType;
    }

    @Override
    public int match(@NonNull Type modelType, @NonNull Type jooqType) {
        if (modelType instanceof ParameterizedType
            && RefectionUtils.isAssignable(List.class, ((ParameterizedType) modelType).getRawType())
            && RefectionUtils.areEquals(RefectionUtils.getComponentTypeOfListOrArray(modelType), _modelType)
            && RefectionUtils.isAssignable(JSON.class, jooqType)) {
            return 11;
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

        CollectionType collectionType = objectMapper.getSerializationConfig()
            .getTypeFactory().constructCollectionType(List.class, _modelType);
        try {
            return objectMapper.readValue(jVal.data(), collectionType);
        } catch (IllegalArgumentException | JsonProcessingException e) {
            throw new MapperConversionException(getJooqType(), getModelType(), e);
        }
    }
}
