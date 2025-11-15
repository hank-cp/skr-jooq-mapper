package org.laxture.skr.jooq.mapper.converter;

import lombok.NonNull;
import org.laxture.skr.jooq.mapper.misc.TypeHelper;

import java.lang.reflect.Type;

public class DirectConverter implements SkrJooqConverter<String, String> {

    @Override
    public int match(Type modelType, Type jooqType) {
        if (TypeHelper.isAssignable(getJooqType(), getModelType())) return 0;
        return MISMATCH;
    }

    @Override
    public String convertToJooqType(@NonNull String mVal) {
        return mVal;
    }

    @Override
    public String convertToModelType(@NonNull String jVal) {
        return jVal;
    }
}
