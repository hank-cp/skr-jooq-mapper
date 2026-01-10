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
     * Converts a camelCase field name to the target case type.
     *
     * @param tableFieldCaseType the target case type
     * @param fieldName the camelCase field name
     * @return the converted field name
     */
    public static String convertFromCamelCase(TableFieldCaseType tableFieldCaseType,
                                              String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) return fieldName;

        switch (tableFieldCaseType) {
            case CAMEL_CASE:
                return fieldName;
            case PASCAL_CASE:
                return convertToPascalCase(fieldName);
            case SNAKE_CASE:
                return camelToDelimitedCase(fieldName, '_', false);
            case SCREAMING_SNAKE_CASE:
                return camelToDelimitedCase(fieldName, '_', true);
            case KEBAB_CASE:
                return camelToDelimitedCase(fieldName, '-', false);
            default:
                return fieldName;
        }
    }

    /**
     * Converts a camelCase or PascalCase field name to PascalCase.
     *
     * @param fieldName the camelCase or PascalCase field name
     * @return the converted field name in PascalCase
     */
    public static String convertToPascalCase(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) return fieldName;
        return Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    /**
     * Converts a PascalCase field name to camelCase.
     *
     * @param fieldName the PascalCase field name
     * @return the converted field name in camelCase
     */
    public static String convertFromPascalCase(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) return fieldName;
        return Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    /**
     * Converts camelCase to a delimited case format (snake_case, SCREAMING_SNAKE_CASE, or kebab-case).
     *
     * @param camelCase the camelCase string
     * @param delimiter the delimiter character ('_' or '-')
     * @param uppercase whether to convert to uppercase
     * @return the converted string
     */
    private static String camelToDelimitedCase(String camelCase, char delimiter, boolean uppercase) {
        if (camelCase == null || camelCase.isEmpty()) return camelCase;

        StringBuilder result = new StringBuilder(camelCase.length() + 5);

        for (int i = 0; i < camelCase.length(); i++) {
            char ch = camelCase.charAt(i);

            if (Character.isUpperCase(ch)) {
                // Add delimiter before uppercase letter (except at the beginning)
                if (i > 0 && shouldAddDelimiter(camelCase, i)) {
                    result.append(delimiter);
                }
                result.append(uppercase ? ch : Character.toLowerCase(ch));
            } else {
                result.append(uppercase ? Character.toUpperCase(ch) : ch);
            }
        }

        return result.toString();
    }

    /**
     * Determines whether to add a delimiter before the current uppercase character.
     * Handles consecutive uppercase letters (e.g., "XMLParser" -> "xml_parser" not "x_m_l_parser")
     *
     * @param str the input string
     * @param index the current index
     * @return true if delimiter should be added
     */
    private static boolean shouldAddDelimiter(String str, int index) {
        // Always add delimiter if previous char is lowercase
        if (Character.isLowerCase(str.charAt(index - 1))) {
            return true;
        }

        // For consecutive uppercase letters, add delimiter before the last one
        // if it's followed by a lowercase letter (e.g., "XMLParser" -> "XML_Parser")
        if (index + 1 < str.length() && Character.isLowerCase(str.charAt(index + 1))) {
            return true;
        }

        return false;
    }

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
            case PASCAL_CASE:
                return convertFromPascalCase(fieldName);
            case SNAKE_CASE:
            case SCREAMING_SNAKE_CASE:
                String[] parts = fieldName.split("_");
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < parts.length; i++) {
                    if (!parts[i].isEmpty()) {
                        if (result.length() == 0) {
                            result.append(parts[i].toLowerCase());
                        } else {
                            result.append(Character.toUpperCase(parts[i].charAt(0)))
                                .append(parts[i].substring(1).toLowerCase());
                        }
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