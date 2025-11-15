package org.laxture.skr.jooq.mapper.converter.jsr310;

import lombok.NonNull;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.misc.MapperConversionException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTime2StringConverter implements SkrJooqConverter<LocalDateTime, String> {

    private static final String FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";

    private final DateTimeFormatter formatter;

    public LocalDateTime2StringConverter() {
        this.formatter = DateTimeFormatter.ofPattern(FORMAT_DATETIME);
    }

    public LocalDateTime2StringConverter(String dateTimeFormat) {
        this.formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
    }

    @Override
    public String convertToJooqType(@NonNull LocalDateTime mVal) {
        return formatter.format(mVal);
    }

    @Override
    public LocalDateTime convertToModelType(@NonNull String jVal) {
        try {
            return LocalDateTime.parse(jVal, formatter);
        } catch (DateTimeParseException e) {
            throw new MapperConversionException(getJooqType(), getModelType(), e);
        }
    }
}
