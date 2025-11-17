package org.laxture.skr.jooq.mapper.converter;

import lombok.NonNull;

import java.lang.reflect.Type;
import java.math.BigInteger;

//public class ArrayConverter implements SkrJooqConverter<Object, Object[]> {
//
//    @Override
//    public int match(Type modelType, Type jooqType) {
//        return SkrJooqConverter.super.match(modelType, jooqType);
//    }
//
//    @Override
//    public Object[] convertToJooqType(@NonNull Object mVal) {
//        return BigInteger.valueOf(mVal);
//    }
//
//    @Override
//    public Object convertToModelType(@NonNull Object[] jVal) {
//        return jVal.longValue();
//    }
//}
