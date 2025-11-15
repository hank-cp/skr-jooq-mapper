package org.laxture.skr.jooq.mapper.misc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import org.laxture.skr.jooq.mapper.annotation.JsonTransient;

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
