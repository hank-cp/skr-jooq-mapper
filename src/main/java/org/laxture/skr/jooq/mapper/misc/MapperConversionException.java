package org.laxture.skr.jooq.mapper.misc;

import java.lang.reflect.Type;

public class MapperConversionException extends RuntimeException {

    public MapperConversionException(Type sourceType, Type targetType) {
        super("Cannot convert " + sourceType.getTypeName() + " to " + targetType.getTypeName());
    }

    public MapperConversionException(Type sourceType, Type targetType, Throwable cause) {
        super("Cannot convert " + sourceType.getTypeName() + " to " + targetType.getTypeName()
                + ": " + cause.getMessage(), cause);
    }

}
