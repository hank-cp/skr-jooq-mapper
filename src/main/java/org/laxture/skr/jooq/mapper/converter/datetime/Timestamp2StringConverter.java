package org.laxture.skr.jooq.mapper.converter.datetime;

import lombok.NonNull;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.misc.MapperConversionException;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Timestamp2StringConverter implements SkrJooqConverter<Timestamp, String> {

    private static final String FORMAT_TIMESTAMP = "yyyy-MM-dd HH:mm:ss";

    private final SimpleDateFormat formatter;

    public Timestamp2StringConverter() {
        this.formatter = new SimpleDateFormat(FORMAT_TIMESTAMP);
    }

    public Timestamp2StringConverter(String timestampFormat) {
        this.formatter = new SimpleDateFormat(timestampFormat);
    }

    @Override
    public String convertToJooqType(@NonNull Timestamp mVal, Class<?> jooqType) {
        return formatter.format(mVal);
    }

    @Override
    public Timestamp convertToModelType(@NonNull String jVal, Type modelType) {
        try {
            return new Timestamp(formatter.parse(jVal).getTime());
        } catch (ParseException e) {
            throw new MapperConversionException(getJooqType(), getModelType(), e);
        }
    }
}
