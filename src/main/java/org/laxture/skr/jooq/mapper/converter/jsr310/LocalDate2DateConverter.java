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

import java.lang.reflect.Type;
import java.sql.Date;
import java.time.LocalDate;

/**
 * Converter for converting LocalDate to Date.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class LocalDate2DateConverter implements SkrJooqConverter<LocalDate, Date> {

    @Override
    public Date convertToJooqType(@NonNull LocalDate mVal, Class<?> jooqType) {
        return Date.valueOf(mVal);
    }

    @Override
    public LocalDate convertToModelType(@NonNull Date jVal, Type modelType) {
        return jVal.toLocalDate();
    }
}
