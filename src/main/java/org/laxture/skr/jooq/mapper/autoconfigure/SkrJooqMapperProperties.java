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

import lombok.Getter;
import lombok.Setter;
import org.laxture.skr.jooq.mapper.TableFieldCaseType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Skr Jooq Mapper.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "skr.jooq.mapper")
public class SkrJooqMapperProperties {

    /**
     * Table field case type for field name conversion.
     * Default is SNAKE_CASE.
     */
    private TableFieldCaseType tableFieldCaseType = TableFieldCaseType.SNAKE_CASE;

    /**
     * Whether to enable Skr Jooq Mapper auto configuration.
     * Default is true.
     */
    private boolean enabled = true;

}
