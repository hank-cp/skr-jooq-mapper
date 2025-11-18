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
package org.laxture.skr.jooq.mapper.misc;

import org.laxture.skr.jooq.mapper.TableFieldCaseType;

/**
 * Utility class for naming operations.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class NamingUtils {

    /**
     * Converts the value to target type.
     *
     * @param tableFieldCaseType the tableFieldCaseType
     * @param fieldName the fieldName
     * @return the result
     */
    public static String convertToCamelCase(TableFieldCaseType tableFieldCaseType,
                                            String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) return fieldName;

        switch (tableFieldCaseType) {
            case CAMEL_CASE:
                return fieldName;
            case SNAKE_CASE:
            case SCREAMING_SNAKE_CASE:
                String[] parts = fieldName.split("_");
                StringBuilder result = new StringBuilder(parts[0].toLowerCase());
                for (int i = 1; i < parts.length; i++) {
                    if (!parts[i].isEmpty()) {
                        result.append(Character.toUpperCase(parts[i].charAt(0)))
                            .append(parts[i].substring(1).toLowerCase());
                    }
                }
                return result.toString();
            case KEBAB_CASE:
                String[] kebabParts = fieldName.split("-");
                StringBuilder kebabResult = new StringBuilder(kebabParts[0].toLowerCase());
                for (int i = 1; i < kebabParts.length; i++) {
                    if (!kebabParts[i].isEmpty()) {
                        kebabResult.append(Character.toUpperCase(kebabParts[i].charAt(0)))
                            .append(kebabParts[i].substring(1).toLowerCase());
                    }
                }
                return kebabResult.toString();
            default:
                return fieldName;
        }
    }
}
