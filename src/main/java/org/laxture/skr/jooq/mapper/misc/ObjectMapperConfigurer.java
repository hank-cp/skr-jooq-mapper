/*
 * Copyright (C) 2019-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/**
 * Utility class for objectmapper operations.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class ObjectMapperConfigurer {

    /**
     * Sets the uppersistentobjectmapper.
     *
     * @param objectMapper the objectMapper
     * @return the result
     */
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
