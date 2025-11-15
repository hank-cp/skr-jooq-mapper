package org.laxture.skr.jooq.mapper;

public enum TableFieldCaseType {

    CAMEL_CASE("camelCase"),
    SCREAMING_SNAKE_CASE("SCREAMING_SNAKE_CASE"),
    SNAKE_CASE("snake_case"),
    KEBAB_CASE("kebab-case");

    private final String value;

    TableFieldCaseType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
