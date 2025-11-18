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
package org.laxture.skr.jooq.mapper.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.laxture.skr.jooq.mapper.DSLContextProvider;
import org.laxture.skr.jooq.mapper.SkrRecordMapperProvider;
import org.laxture.skr.jooq.mapper.SkrRecordUnmapperProvider;
import org.laxture.skr.jooq.mapper.TableFieldCaseType;
import org.laxture.skr.jooq.mapper.converter.ConverterRegistry;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.converter.json.*;
import org.laxture.skr.jooq.mapper.misc.ObjectMapperConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Spring Boot auto-configuration for Skr Jooq Mapper.
 * <p>
 * This configuration automatically sets up the necessary beans for Jooq record mapping
 * when DSLContext is available in the Spring context.
 * <p>
 * The configuration can be customized through application properties with prefix "skr.jooq.mapper":
 * <ul>
 *   <li>enabled: Whether to enable auto-configuration (default: true)</li>
 *   <li>tableFieldCaseType: Field naming convention (default: SNAKE_CASE)</li>
 * </ul>
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@Slf4j
@Configuration
@ConditionalOnClass({DSLContext.class, ObjectMapper.class})
@ConditionalOnProperty(prefix = "skr.jooq.mapper", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SkrJooqMapperProperties.class)
@AutoConfigureAfter({JooqAutoConfiguration.class, JacksonAutoConfiguration.class})
public class SkrJooqMapperAutoConfiguration {

    /**
     * Creates and configures the ConverterRegistry bean.
     * <p>
     * Registers all built-in converters and any custom converters provided by the application.
     */
    @Bean
    @ConditionalOnMissingBean
    public ConverterRegistry converterRegistry(@Autowired ObjectMapper objectMapper,
                                               @Autowired(required = false) List<SkrJooqConverter<?, ?>> customConverters) {
        ConverterRegistry registry = new ConverterRegistry();

        ObjectMapper cookedObjectMapper = ObjectMapperConfigurer.setupPersistentObjectMapper(objectMapper.copy());

        // Register built-in converters
        // Note: Some converters are already registered in ConverterRegistry constructor
        // Here we register the remaining json converters that require ObjectMapper
        registry.registerConverter(new JsonArrayConverter(cookedObjectMapper), null);
        registry.registerConverter(new JsonObjectConverter(cookedObjectMapper), null);
        registry.registerConverter(new JsonObject2MapConverter(cookedObjectMapper), null);
        registry.registerConverter(new JsonbArrayConverter(cookedObjectMapper), null);
        registry.registerConverter(new JsonbObjectConverter(cookedObjectMapper), null);
        registry.registerConverter(new JsonbObject2MapConverter(cookedObjectMapper), null);

        // Register custom converters if any
        if (customConverters != null && !customConverters.isEmpty()) {
            log.info("Registering {} custom Jooq converters", customConverters.size());
            customConverters.forEach(converter ->
                registry.registerConverter(converter, "custom"));
        }

        log.info("ConverterRegistry initialized with built-in and custom converters");
        return registry;
    }

    /**
     * Creates a DSLContextProvider bean from the Spring-managed DSLContext.
     * <p>
     * This provider is used by the unmapper to create new record instances.
     *
     * @param dslContext the Spring-managed DSLContext
     * @return DSLContextProvider instance
     */
    @Bean
    @ConditionalOnBean(DSLContext.class)
    @ConditionalOnMissingBean
    public DSLContextProvider dslContextProvider(DSLContext dslContext) {
        log.info("Creating DSLContextProvider with Spring-managed DSLContext");
        return () -> dslContext;
    }

    /**
     * Creates an ObjectMapper for persistence operations.
     * <p>
     * If a Spring-managed ObjectMapper exists, it will be used and configured for persistence.
     * Otherwise, a new ObjectMapper will be created with persistence-specific settings.
     *
     * @param springObjectMapper optional Spring-managed ObjectMapper
     * @return configured ObjectMapper for persistence
     */
    @Bean(name = "skrJooqMapperObjectMapper")
    @ConditionalOnMissingBean(name = "skrJooqMapperObjectMapper")
    public ObjectMapper skrJooqMapperObjectMapper(
            @Autowired(required = false) ObjectMapper springObjectMapper) {

        if (springObjectMapper != null) {
            log.info("Using Spring-managed ObjectMapper for Skr Jooq Mapper");
            return ObjectMapperConfigurer.setupPersistentObjectMapper(springObjectMapper);
        } else {
            log.info("Creating new ObjectMapper for Skr Jooq Mapper");
            ObjectMapper objectMapper = new ObjectMapper();
            return ObjectMapperConfigurer.setupPersistentObjectMapper(objectMapper);
        }
    }

    /**
     * Creates the SkrRecordUnmapperProvider bean.
     * <p>
     * This provider is responsible for creating RecordUnmapper instances that convert
     * model objects to Jooq records.
     */
    @Bean
    @ConditionalOnMissingBean
    public SkrRecordUnmapperProvider skrRecordUnmapperProvider(
            DSLContextProvider dslContextProvider,
            ConverterRegistry converterRegistry,
            SkrJooqMapperProperties properties,
            ObjectMapper skrJooqMapperObjectMapper) {

        TableFieldCaseType caseType = properties.getTableFieldCaseType();
        log.info("Creating SkrRecordUnmapperProvider with tableFieldCaseType: {}", caseType);

        return new SkrRecordUnmapperProvider(
            dslContextProvider,
            converterRegistry,
            caseType,
            skrJooqMapperObjectMapper
        );
    }
}
