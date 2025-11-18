package org.laxture.skr.jooq.mapper.converter.jsr310;

import lombok.NonNull;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;

import java.lang.reflect.Type;
import java.sql.Time;
import java.time.LocalTime;

public class LocalTime2TimeConverter implements SkrJooqConverter<LocalTime, Time> {

    @Override
    public Time convertToJooqType(@NonNull LocalTime mVal, Class<?> jooqType) {
        return Time.valueOf(mVal);
    }

    @Override
    public LocalTime convertToModelType(@NonNull Time jVal, Type modelType) {
        return jVal.toLocalTime();
    }
}
