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
