/*
 * Copyright (C) 2019-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.laxture.skr.jooq.mapper.converter.datetime;

import lombok.NonNull;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.misc.MapperConversionException;

import java.lang.reflect.Type;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Converter for converting Time to String.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
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
