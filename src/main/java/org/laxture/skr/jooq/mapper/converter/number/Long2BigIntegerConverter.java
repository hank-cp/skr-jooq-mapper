package org.laxture.skr.jooq.mapper.converter.number;

import lombok.NonNull;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;

import java.math.BigInteger;

public class Long2BigIntegerConverter implements SkrJooqConverter<Long, BigInteger> {

    @Override
    public BigInteger convertToJooqType(@NonNull Long mVal) {
        return BigInteger.valueOf(mVal);
    }

    @Override
    public Long convertToModelType(@NonNull BigInteger jVal) {
        return jVal.longValue();
    }
}
