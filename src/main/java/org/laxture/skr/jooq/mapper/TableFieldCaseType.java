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
package org.laxture.skr.jooq.mapper;

/**
 * TableFieldCaseType implementation.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public enum TableFieldCaseType {

    CAMEL_CASE("camelCase"),
    SCREAMING_SNAKE_CASE("SCREAMING_SNAKE_CASE"),
    SNAKE_CASE("snake_case"),
    KEBAB_CASE("kebab-case");

    private final String value;

    TableFieldCaseType(String value) {
        this.value = value;
    }

    /**
     * Value operation.
     * @return the result
     */
    public String value() {
        return value;
    }

}
