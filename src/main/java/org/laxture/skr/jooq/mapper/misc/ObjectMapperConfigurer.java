package org.laxture.skr.jooq.mapper.misc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.laxture.skr.jooq.mapper.annotation.JsonTransient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ObjectMapperConfigurer {

    public static ObjectMapper setupPersistentObjectMapper(ObjectMapper objectMapper) {
        return objectMapper.copy().configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true)
            .setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
                @Override
                public boolean hasIgnoreMarker(AnnotatedMember m) {
                    return _hasOneOf(m, new Class[]{
                        JsonTransient.class, JsonIgnore.class,
                        java.beans.Transient.class,
//                        jakarta.persistence.Transient.class,
                        org.springframework.data.annotation.Transient.class});
                }
            });
    }
}
