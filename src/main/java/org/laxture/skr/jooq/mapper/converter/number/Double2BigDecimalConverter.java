package org.laxture.skr.jooq.mapper.converter.number;

import lombok.NonNull;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;

import java.math.BigDecimal;

public class Double2BigDecimalConverter implements SkrJooqConverter<Double, BigDecimal> {

    @Override
    public BigDecimal convertToJooqType(@NonNull Double mVal) {
        return BigDecimal.valueOf(mVal);
    }

    @Override
    public Double convertToModelType(@NonNull BigDecimal jVal) {
        return jVal.doubleValue();
    }
}
