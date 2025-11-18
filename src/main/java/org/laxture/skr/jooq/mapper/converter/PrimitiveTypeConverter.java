package org.laxture.skr.jooq.mapper.converter;

import lombok.NonNull;
import org.apache.commons.beanutils.ConvertUtils;
import org.laxture.skr.jooq.mapper.misc.RefectionUtils;

import java.lang.reflect.Type;

public class PrimitiveTypeConverter implements SkrJooqConverter<Object, Object> {

    @Override
    public int match(Type modelType, Type jooqType) {
        if (RefectionUtils.isPrimitive(modelType) && RefectionUtils.isPrimitive(jooqType)) return 1;
        return MISMATCH;
    }

    @Override
    public Object convertToJooqType(@NonNull Object mVal, Class<?> jooqType) {
        return ConvertUtils.convert(mVal, jooqType);
    }

    @Override
    public Object convertToModelType(@NonNull Object jVal, Type modelType) {
        return ConvertUtils.convert(jVal, RefectionUtils.toClass(modelType));
    }
}
