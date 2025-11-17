package org.laxture.skr.jooq.mapper.converter.jsr310;

import lombok.NonNull;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;

import java.sql.Date;
import java.time.LocalDate;

public class LocalDate2DateConverter implements SkrJooqConverter<LocalDate, Date> {

    @Override
    public Date convertToJooqType(@NonNull LocalDate mVal) {
        return Date.valueOf(mVal);
    }

    @Override
    public LocalDate convertToModelType(@NonNull Date jVal) {
        return jVal.toLocalDate();
    }
}
