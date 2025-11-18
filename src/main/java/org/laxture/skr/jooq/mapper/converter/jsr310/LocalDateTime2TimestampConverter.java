package org.laxture.skr.jooq.mapper.converter.jsr310;

import lombok.NonNull;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class LocalDateTime2TimestampConverter implements SkrJooqConverter<LocalDateTime, Timestamp> {

    @Override
    public Timestamp convertToJooqType(@NonNull LocalDateTime mVal, Class<?> jooqType) {
        return Timestamp.valueOf(mVal);
    }

    @Override
    public LocalDateTime convertToModelType(@NonNull Timestamp jVal, Type modelType) {
        return jVal.toLocalDateTime();
    }
}
