package org.laxture.skr.jooq.mapper.misc;

import org.laxture.skr.jooq.mapper.TableFieldCaseType;

public class NamingUtils {

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
