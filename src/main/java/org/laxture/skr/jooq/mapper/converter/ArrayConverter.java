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
import org.apache.commons.beanutils.ConvertUtils;
import org.laxture.skr.jooq.mapper.misc.MapperConversionException;
import org.laxture.skr.jooq.mapper.misc.RefectionUtils;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Converter for type conversion operations.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class ArrayConverter implements SkrJooqConverter<Object, Object> {

    @Override
    public int match(@NonNull Type modelType, @NonNull Type jooqType) {
        if (RefectionUtils.isArray(jooqType) // jooq is always array
                && (RefectionUtils.isArray(modelType) // model could be array or collection
                    || Collection.class.isAssignableFrom(RefectionUtils.toClass(modelType)))) {
            return 11;
        }
        return MISMATCH;
    }

    @Override
    public Object convertToJooqType(@NonNull Object mVal, Class<?> jooqType) {
        if (RefectionUtils.isArray(mVal.getClass())) {
            return ConvertUtils.convert(mVal, jooqType);
        }
        if (mVal instanceof Collection<?> mCollection) {
            return ConvertUtils.convert(mCollection.toArray(), jooqType);
        }
        throw new MapperConversionException(mVal.getClass(), Object[].class);
    }

    @Override
    public Object convertToModelType(Object jVal, Type modelType) {
        Type mElementType = RefectionUtils.getComponentTypeOfListOrArray(modelType);
        if (mElementType == null) {
            throw new MapperConversionException(jVal.getClass(), modelType);
        }
        Class<?> mElementClass = RefectionUtils.toClass(mElementType);

        if (RefectionUtils.isArray(modelType)) {
            return ConvertUtils.convert(jVal, RefectionUtils.toClass(modelType));
        }

        if (List.class.isAssignableFrom(RefectionUtils.toClass(modelType))) {
            return Arrays.stream(RefectionUtils.wrapArray(jVal))
                .map(elem -> ConvertUtils.convert(elem, mElementClass)).
                collect(Collectors.toList());
        }

        if (Set.class.isAssignableFrom(RefectionUtils.toClass(modelType))) {
            return Arrays.stream(RefectionUtils.wrapArray(jVal))
                .map(elem -> ConvertUtils.convert(elem, mElementClass))
                .collect(Collectors.toSet());
        }

        throw new MapperConversionException(jVal.getClass(), modelType);
    }
}
