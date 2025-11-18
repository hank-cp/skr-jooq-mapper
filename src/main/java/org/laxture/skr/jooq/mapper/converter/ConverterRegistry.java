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
package org.laxture.skr.jooq.mapper.converter;

import lombok.NonNull;
import org.laxture.skr.jooq.mapper.converter.datetime.Date2StringConverter;
import org.laxture.skr.jooq.mapper.converter.datetime.Time2StringConverter;
import org.laxture.skr.jooq.mapper.converter.datetime.Timestamp2StringConverter;
import org.laxture.skr.jooq.mapper.converter.jsr310.*;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.laxture.skr.jooq.mapper.converter.SkrJooqConverter.MISMATCH;

/**
 * Registry for managing type converters between Jooq and model types.
 * <p>
 * This registry maintains a collection of converters and provides methods to register,
 * unregister, and find the most suitable converter for a given type pair.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class ConverterRegistry {

    private final Map<SkrJooqConverter<?, ?>, String> converters = Collections.synchronizedMap(new HashMap<>());

    public ConverterRegistry() {
        converters.put(new DirectConverter(), null);
        converters.put(new PrimitiveTypeConverter(), null);
        converters.put(new ArrayConverter(), null);
        converters.put(new Date2StringConverter(), null);
        converters.put(new Time2StringConverter(), null);
        converters.put(new Timestamp2StringConverter(), null);
        converters.put(new LocalDate2StringConverter(), null);
        converters.put(new LocalDateTime2StringConverter(), null);
        converters.put(new LocalTime2StringConverter(), null);
        converters.put(new LocalDate2DateConverter(), null);
        converters.put(new LocalDateTime2TimestampConverter(), null);
        converters.put(new LocalTime2TimeConverter(), null);
    }

    /**
     * Registers a converter with an optional registry key.
     *
     * @param converter the converter to register
     * @param registryKey optional key for later lookup or removal
     */
    public void registerConverter(@NonNull SkrJooqConverter<?, ?> converter, String registryKey) {
        converters.put(converter, registryKey);
    }

    /**
     * Unregisters all converters associated with the given registry key.
     *
     * @param registryKey the registry key
     */
    public void unregisterConverter(@NonNull String registryKey) {
        List<SkrJooqConverter<?, ?>> removeCandidates = converters.entrySet().stream()
            .filter(e -> registryKey.equals(e.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        removeCandidates.forEach(converters::remove);
    }

    /**
     * Finds the best matching converter for the given model and Jooq types.
     * <p>
     * The converter with the highest matching priority is returned.
     *
     * @param modelType the model type
     * @param jooqType the Jooq type
     * @return the best matching converter, or null if no match found
     */
    public SkrJooqConverter<?, ?> matchConverter(Type modelType, Type jooqType) {
        int maxPriority = MISMATCH;
        SkrJooqConverter<?, ?> matchedConverter = null;
        for (SkrJooqConverter<?, ?> converter : converters.keySet()) {
            int priority = converter.match(modelType, jooqType);
            if (priority < 0) continue;
            if (priority > maxPriority) {
                maxPriority = priority;
                matchedConverter = converter;
            }
        }
        return matchedConverter;
    }
}
