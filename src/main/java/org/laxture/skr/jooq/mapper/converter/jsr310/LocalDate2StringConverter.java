package org.laxture.skr.jooq.mapper.converter.jsr310;

import lombok.NonNull;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.misc.MapperConversionException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDate2StringConverter implements SkrJooqConverter<LocalDate, String> {

    private static final String FORMAT_DATE = "yyyy-MM-dd";

    private final DateTimeFormatter formatter;

    public LocalDate2StringConverter() {
        this.formatter = DateTimeFormatter.ofPattern(FORMAT_DATE);
    }

    public LocalDate2StringConverter(String dateFormat) {
        this.formatter = DateTimeFormatter.ofPattern(dateFormat);
    }

    @Override
    public String convertToJooqType(@NonNull LocalDate mVal) {
        return formatter.format(mVal);
    }

    @Override
    public LocalDate convertToModelType(@NonNull String jVal) {
        try {
            return LocalDate.parse(jVal, formatter);
        } catch (DateTimeParseException e) {
            throw new MapperConversionException(getJooqType(), getModelType(), e);
        }
    }
}
