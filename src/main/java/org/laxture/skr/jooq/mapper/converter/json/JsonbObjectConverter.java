package org.laxture.skr.jooq.mapper.converter.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.jooq.JSONB;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.misc.MapperConversionException;
import org.laxture.skr.jooq.mapper.misc.RefectionUtils;

import java.io.IOException;

public class JsonbObjectConverter<ModelType> implements SkrJooqConverter<ModelType, JSONB> {

    private final ObjectMapper objectMapper;
    @Getter
    protected final Class<ModelType> modelType;

    public JsonbObjectConverter(ObjectMapper objectMapper, Class<ModelType> modelType) {
        this.objectMapper = objectMapper;
        this.modelType = modelType;
    }

    @Override
    public JSONB convertToJooqType(ModelType mVal) {
        try {
            return JSONB.valueOf(objectMapper.writeValueAsString(mVal));
        } catch (IOException e) {
            throw new MapperConversionException(getModelType(), getJooqType(), e);
        }
    }

    @Override
    public ModelType convertToModelType(JSONB jVal) {
        if ("null".equals(jVal.toString())) return null;
        try {
            return objectMapper.readValue(jVal.data(), RefectionUtils.toClass(getModelType()));
        } catch (IllegalArgumentException | JsonProcessingException e) {
            throw new MapperConversionException(getJooqType(), getModelType(), e);
        }
    }
}
