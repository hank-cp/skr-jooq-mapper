package org.laxture.skr.jooq.mapper.converter;

import lombok.NonNull;
import org.laxture.skr.jooq.mapper.misc.TypeHelper;

import java.lang.reflect.Type;

public interface SkrJooqConverter<ModelType, JooqType> {

    int MISMATCH = -1;

    default Type getModelType() {
        Type[] genericTypes = TypeHelper.getGenericParameterForClass(this.getClass(), SkrJooqConverter.class);
        assert genericTypes != null && genericTypes.length == 2;
        return genericTypes[0];
    }

    default Type getJooqType() {
        Type[] genericTypes = TypeHelper.getGenericParameterForClass(this.getClass(), SkrJooqConverter.class);
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
        if (TypeHelper.isAssignable(getModelType(), modelType)
            && TypeHelper.isAssignable(getJooqType(), jooqType)) return 10;
        return MISMATCH;
    }

    JooqType convertToJooqType(@NonNull ModelType mVal);

    ModelType convertToModelType(@NonNull JooqType jVal);
}
