package org.laxture.skr.jooq.mapper.converter.datetime;

import lombok.NonNull;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.misc.MapperConversionException;

import java.lang.reflect.Type;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Time2StringConverter implements SkrJooqConverter<Time, String> {

    private static final String FORMAT_TIME = "HH:mm:ss";

    private final SimpleDateFormat formatter;

    public Time2StringConverter() {
        this.formatter = new SimpleDateFormat(FORMAT_TIME);
    }

    public Time2StringConverter(String timeFormat) {
        this.formatter = new SimpleDateFormat(timeFormat);
    }

    @Override
    public String convertToJooqType(@NonNull Time mVal, Class<?> jooqType) {
        return formatter.format(mVal);
    }

    @Override
    public Time convertToModelType(@NonNull String jVal, Type modelType) {
        try {
            return new Time(formatter.parse(jVal).getTime());
        } catch (ParseException e) {
            throw new MapperConversionException(getJooqType(), getModelType(), e);
        }
    }
}
