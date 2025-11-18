package org.laxture.skr.jooq.mapper.converter.datetime;

import lombok.NonNull;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.misc.MapperConversionException;

import java.lang.reflect.Type;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Date2StringConverter implements SkrJooqConverter<Date, String> {

    private static final String FORMAT_DATE = "yyyy-MM-dd";

    private final SimpleDateFormat formatter;

    public Date2StringConverter() {
        this.formatter = new SimpleDateFormat(FORMAT_DATE);
    }

    public Date2StringConverter(String dateFormat) {
        this.formatter = new SimpleDateFormat(FORMAT_DATE);
    }

    @Override
    public String convertToJooqType(@NonNull Date mVal, Class<?> jooqType) {
        return formatter.format(mVal);
    }

    @Override
    public Date convertToModelType(@NonNull String jVal, Type modelType) {
        try {
            return new Date(formatter.parse(jVal).getTime());
        } catch (ParseException e) {
            throw new MapperConversionException(getJooqType(), getModelType(), e);
        }
    }
}
