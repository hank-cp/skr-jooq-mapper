package org.laxture.skr.jooq.mapper.converter.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.JSON;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.misc.MapperConversionException;
import org.laxture.skr.jooq.mapper.misc.TypeHelper;

import java.io.IOException;

public class JsonObjectConverter<ModelType> implements SkrJooqConverter<ModelType, JSON> {

    private final ObjectMapper objectMapper;

    public JsonObjectConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public JSON convertToJooqType(ModelType mVal) {
        try {
            return JSON.valueOf(objectMapper.writeValueAsString(mVal));
        } catch (IOException e) {
            throw new MapperConversionException(getModelType(), getJooqType(), e);
        }
    }

    @Override
    public ModelType convertToModelType(JSON jVal) {
        if ("null".equals(jVal.toString())) return null;
        try {
            return objectMapper.convertValue(jVal, TypeHelper.toClass(getModelType()));
        } catch (IllegalArgumentException e) {
            throw new MapperConversionException(getJooqType(), getModelType(), e);
        }
    }
}
