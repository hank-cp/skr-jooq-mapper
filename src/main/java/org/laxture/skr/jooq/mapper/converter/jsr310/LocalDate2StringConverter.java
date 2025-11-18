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
package org.laxture.skr.jooq.mapper.converter.jsr310;

import lombok.NonNull;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.misc.MapperConversionException;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Converter for converting LocalDate to String.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class LocalDate2StringConverter implements SkrJooqConverter<LocalDate, String> {

    private static final String FORMAT_DATE = "yyyy-MM-dd";

    private final DateTimeFormatter formatter;

    public LocalDate2StringConverter() {
        this.formatter = DateTimeFormatter.ofPattern(FORMAT_DATE);
    }

    public LocalDate2StringConverter(String dateFormat) {
        this.formatter = DateTimeFormatter.ofPattern(dateFormat);
    }

    @Override
    public String convertToJooqType(@NonNull LocalDate mVal, Class<?> jooqType) {
        return formatter.format(mVal);
    }

    @Override
    public LocalDate convertToModelType(@NonNull String jVal, Type modelType) {
        try {
            return LocalDate.parse(jVal, formatter);
        } catch (DateTimeParseException e) {
            throw new MapperConversionException(getJooqType(), getModelType(), e);
        }
    }
}
