package org.laxture.skr.jooq.mapper.converter;

import lombok.NonNull;
import org.laxture.skr.jooq.mapper.converter.datetime.Date2StringConverter;
import org.laxture.skr.jooq.mapper.converter.datetime.Time2StringConverter;
import org.laxture.skr.jooq.mapper.converter.datetime.Timestamp2StringConverter;
import org.laxture.skr.jooq.mapper.converter.jsr310.*;
import org.laxture.skr.jooq.mapper.converter.number.Double2BigDecimalConverter;
import org.laxture.skr.jooq.mapper.converter.number.Long2BigIntegerConverter;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.laxture.skr.jooq.mapper.converter.SkrJooqConverter.MISMATCH;

public class ConverterRegistry {

    private final Map<SkrJooqConverter<?, ?>, String> converters = Collections.synchronizedMap(new HashMap<>());

    public ConverterRegistry() {
        converters.put(new DirectConverter(), null);
        converters.put(new Double2BigDecimalConverter(), null);
        converters.put(new Long2BigIntegerConverter(), null);
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

    public void registerConverter(@NonNull SkrJooqConverter<?, ?> converter, String registryKey) {
        converters.put(converter, registryKey);
    }

    public void unregisterConverter(@NonNull String registryKey) {
        List<SkrJooqConverter<?, ?>> removeCandidates = converters.entrySet().stream()
            .filter(e -> registryKey.equals(e.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        removeCandidates.forEach(converters::remove);
    }

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
