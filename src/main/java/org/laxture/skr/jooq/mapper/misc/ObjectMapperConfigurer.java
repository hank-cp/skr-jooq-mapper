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

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Utility class for objectmapper operations.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class ObjectMapperConfigurer {

    private static final String[] IGNORE_ANNOTATION_NAMES = new String[]{
        org.laxture.skr.jooq.mapper.annotation.JsonTransient.class.getName(),
        com.fasterxml.jackson.annotation.JsonIgnore.class.getName(),
        java.beans.Transient.class.getName(),
        org.springframework.data.annotation.Transient.class.getName(),
        "jakarta.persistence.Transient",
        "javax.persistence.Transient"
    };

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
                    for (java.lang.annotation.Annotation annotation : m.getAllAnnotations().annotations()) {
                        if (ArrayUtils.contains(IGNORE_ANNOTATION_NAMES, annotation.annotationType().getName())) return true;
                    }
                    return false;
                }
            });
    }
}
