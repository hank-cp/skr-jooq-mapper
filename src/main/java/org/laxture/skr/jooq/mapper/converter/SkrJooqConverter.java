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
package org.laxture.skr.jooq.mapper.converter;

import lombok.NonNull;
import org.laxture.skr.jooq.mapper.misc.RefectionUtils;

import java.lang.reflect.Type;

/**
 * Converter interface for type conversion.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public interface SkrJooqConverter<ModelType, JooqType> {

    int MISMATCH = -1;

    default Type getModelType() {
        Type[] genericTypes = RefectionUtils.getGenericParameterForClass(this.getClass(), SkrJooqConverter.class);
        assert genericTypes != null && genericTypes.length == 2;
        return genericTypes[0];
    }

    default Type getJooqType() {
        Type[] genericTypes = RefectionUtils.getGenericParameterForClass(this.getClass(), SkrJooqConverter.class);
        assert genericTypes != null && genericTypes.length == 2;
        return genericTypes[1];
    }

    /**
     * Check if the converter matches the model and jooq value types, and return a matching priority
     * @return negative value if the converter does not match the model and jooq value types.
     *         The maximum priority converter will be used.
     *         Built-in converters come with priority 10. Return priority above 10 will override the built-in converters.
     */
    default int match(Type modelType, Type jooqType) {
        if (RefectionUtils.isAssignable(getModelType(), modelType)
            && RefectionUtils.isAssignable(getJooqType(), jooqType)) return 10;
        return MISMATCH;
    }

    /**
     * Check if the converter is cacheable.
     * @return true if the converter is cacheable.
     */
    default boolean cacheable() {
        return true;
    }

    /**
     * Convert model value to jooq value.
     * @param mVal the model value to convert.
     * @param jooqType the jooq type to convert to.
     * @return the converted jooq value.
     */
    JooqType convertToJooqType(@NonNull ModelType mVal, Class<?> jooqType);

    /**
     * Convert jooq value to model value.
     * @param jVal the jooq value to convert.
     * @param modelType the model type to convert to.
     * @return the converted model value.
     */
    ModelType convertToModelType(@NonNull JooqType jVal, Type modelType);
}
