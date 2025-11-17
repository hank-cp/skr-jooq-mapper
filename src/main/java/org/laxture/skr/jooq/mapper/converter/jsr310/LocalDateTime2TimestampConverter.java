package org.laxture.skr.jooq.mapper.converter.jsr310;

import lombok.NonNull;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class LocalDateTime2TimestampConverter implements SkrJooqConverter<LocalDateTime, Timestamp> {

    @Override
    public Timestamp convertToJooqType(@NonNull LocalDateTime mVal) {
        return Timestamp.valueOf(mVal);
    }

    @Override
    public LocalDateTime convertToModelType(@NonNull Timestamp jVal) {
        return jVal.toLocalDateTime();
    }
}
