package org.laxture.skr.jooq.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.misc.ObjectMapperConfigurer;

public class JsonNodeConverter implements SkrJooqConverter<JsonNode, Object> {

    @Override
    public Object convertToJooqType(JsonNode mVal) {
        if (mVal == null) return null;
        try {
            return ObjectMapperConfigurer.getGeneralMapper().writeValueAsString(mVal);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JsonNode to String", e);
        }
    }

    @Override
    public JsonNode convertToModelType(Object jVal) {
        if (jVal == null) return null;
        if (jVal instanceof JsonNode) return (JsonNode) jVal;

        try {
            return ObjectMapperConfigurer.getGeneralMapper().readTree(jVal.toString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert String to JsonNode", e);
        }
    }
}
