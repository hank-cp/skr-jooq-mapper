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
 * Converter for type conversion operations.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class DirectConverter implements SkrJooqConverter<Object, Object> {

    @Override
    public int match(Type modelType, Type jooqType) {
        if (RefectionUtils.isAssignable(getJooqType(), getModelType())) return 0;
        return MISMATCH;
    }

    @Override
    public Object convertToJooqType(@NonNull Object mVal, Class<?> jooqType) {
        return mVal;
    }

    @Override
    public Object convertToModelType(@NonNull Object jVal, Type modelType) {
        return jVal;
    }
}
