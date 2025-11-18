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
import java.sql.Time;
import java.time.LocalTime;

/**
 * Converter for converting LocalTime to Time.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
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
