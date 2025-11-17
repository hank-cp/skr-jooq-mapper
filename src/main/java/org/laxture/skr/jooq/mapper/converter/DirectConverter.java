package org.laxture.skr.jooq.mapper.converter;

import lombok.NonNull;
import org.laxture.skr.jooq.mapper.misc.RefectionUtils;

import java.lang.reflect.Type;

public class DirectConverter implements SkrJooqConverter<Object, Object> {

    @Override
    public int match(Type modelType, Type jooqType) {
        if (RefectionUtils.isAssignable(getJooqType(), getModelType())) return 0;
        return MISMATCH;
    }

    @Override
    public Object convertToJooqType(@NonNull Object mVal) {
        return mVal;
    }

    @Override
    public Object convertToModelType(@NonNull Object jVal) {
        return jVal;
    }
}
