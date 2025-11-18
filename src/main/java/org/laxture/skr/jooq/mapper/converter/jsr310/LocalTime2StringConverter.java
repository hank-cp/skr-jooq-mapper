package org.laxture.skr.jooq.mapper.converter.jsr310;

import lombok.NonNull;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.misc.MapperConversionException;

import java.lang.reflect.Type;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalTime2StringConverter implements SkrJooqConverter<LocalTime, String> {

    private static final String FORMAT_TIME = "HH:mm:ss";

    private final DateTimeFormatter formatter;

    public LocalTime2StringConverter() {
        this.formatter = DateTimeFormatter.ofPattern(FORMAT_TIME);
    }

    public LocalTime2StringConverter(String timeFormat) {
        this.formatter = DateTimeFormatter.ofPattern(timeFormat);
    }

    @Override
    public String convertToJooqType(@NonNull LocalTime mVal, Class<?> jooqType) {
        return formatter.format(mVal);
    }

    @Override
    public LocalTime convertToModelType(@NonNull String jVal, Type modelType) {
        try {
            return LocalTime.parse(jVal, formatter);
        } catch (DateTimeParseException e) {
            throw new MapperConversionException(getJooqType(), getModelType(), e);
        }
    }
}
