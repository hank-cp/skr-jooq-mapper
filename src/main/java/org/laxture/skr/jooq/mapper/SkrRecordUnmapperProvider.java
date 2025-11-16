package org.laxture.skr.jooq.mapper;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.jooq.Record;
import org.laxture.skr.jooq.mapper.converter.ConverterRegistry;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SkrRecordUnmapperProvider implements RecordUnmapperProvider {

    private final DSLContextProvider dslContextProvider;
    private final ConverterRegistry converterRegistry;
    private final TableFieldCaseType tableFieldCaseType;

    private final List<SkrJooqConverter<?, ?>> customConverters = new ArrayList<>();

    public SkrRecordUnmapperProvider(DSLContextProvider dslContextProvider,
                                     ConverterRegistry converterRegistry,
                                     TableFieldCaseType tableFieldCaseType) {
        this.dslContextProvider = dslContextProvider;
        this.converterRegistry = converterRegistry;
        this.tableFieldCaseType = tableFieldCaseType;
    }

    @Override
    public <E, R extends Record> RecordUnmapper<E, R> provide(Class<? extends E> type, RecordType<R> recordType) {
        return new SkrRecordUnmapper<>(recordType);
    }

    private class SkrRecordUnmapper<E, R extends Record> implements RecordUnmapper<E, R> {
        private final RecordType<R> recordType;

        SkrRecordUnmapper(RecordType<R> recordType) {
            this.recordType = recordType;
        }

        @Override
        public R unmap(@NonNull E model) {
            R record = (R) dslContextProvider.provide().newRecord(recordType.fields());

            for (Field<?> field : record.fields()) {
                // TODO 1. convert field name to snake case
                // TODO 2. locate model field recursively, including @LeftoverCollector map
                // TODO 3. convert value and set to record
            }

            return record;
        }
    }

    @SuppressWarnings("unchecked")
    private <ModelType, JooqType> JooqType convertFieldValue(ModelType mVal, Class<?> jooqType) {
        if (mVal == null) return null;

        SkrJooqConverter<ModelType, JooqType> converter =
            (SkrJooqConverter<ModelType, JooqType>) converterRegistry.matchConverter(mVal.getClass(), jooqType);

        if (converter == null) {
            log.warn("No converter found for jooq type {} and model type {}", jooqType, mVal.getClass());
            return null;
        }

        return converter.convertToJooqType(mVal);
    }
}
