package org.laxture.skr.jooq.test;

import org.junit.jupiter.api.Test;
import org.laxture.skr.jooq.mapper.TableFieldCaseType;
import org.laxture.skr.jooq.mapper.misc.NamingUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

class NamingUtilsTest {

    @Test
    void testConvertToCamelCase_FromSnakeCase() {
        assertThat(NamingUtils.convertToCamelCase(TableFieldCaseType.SNAKE_CASE, "user_name"),
            equalTo("userName"));
        assertThat(NamingUtils.convertToCamelCase(TableFieldCaseType.SNAKE_CASE, "first_name_last_name"),
            equalTo("firstNameLastName"));
        assertThat(NamingUtils.convertToCamelCase(TableFieldCaseType.SNAKE_CASE, "id"),
            equalTo("id"));
        assertThat(NamingUtils.convertToCamelCase(TableFieldCaseType.SNAKE_CASE, "user_id"),
            equalTo("userId"));
    }

    @Test
    void testConvertToCamelCase_FromScreamingSnakeCase() {
        assertThat(NamingUtils.convertToCamelCase(TableFieldCaseType.SCREAMING_SNAKE_CASE, "USER_NAME"),
            equalTo("userName"));
        assertThat(NamingUtils.convertToCamelCase(TableFieldCaseType.SCREAMING_SNAKE_CASE, "FIRST_NAME_LAST_NAME"),
            equalTo("firstNameLastName"));
        assertThat(NamingUtils.convertToCamelCase(TableFieldCaseType.SCREAMING_SNAKE_CASE, "ID"),
            equalTo("id"));
    }

    @Test
    void testConvertToCamelCase_FromKebabCase() {
        assertThat(NamingUtils.convertToCamelCase(TableFieldCaseType.KEBAB_CASE, "user-name"),
            equalTo("userName"));
        assertThat(NamingUtils.convertToCamelCase(TableFieldCaseType.KEBAB_CASE, "first-name-last-name"),
            equalTo("firstNameLastName"));
        assertThat(NamingUtils.convertToCamelCase(TableFieldCaseType.KEBAB_CASE, "id"),
            equalTo("id"));
    }

    @Test
    void testConvertToCamelCase_FromCamelCase() {
        assertThat(NamingUtils.convertToCamelCase(TableFieldCaseType.CAMEL_CASE, "userName"),
            equalTo("userName"));
        assertThat(NamingUtils.convertToCamelCase(TableFieldCaseType.CAMEL_CASE, "firstName"),
            equalTo("firstName"));
    }

    @Test
    void testConvertToCamelCase_EdgeCases() {
        assertThat(NamingUtils.convertToCamelCase(TableFieldCaseType.SNAKE_CASE, null), nullValue());
        assertThat(NamingUtils.convertToCamelCase(TableFieldCaseType.SNAKE_CASE, ""), equalTo(""));
        assertThat(NamingUtils.convertToCamelCase(TableFieldCaseType.SNAKE_CASE, "a"), equalTo("a"));
        assertThat(NamingUtils.convertToCamelCase(TableFieldCaseType.SNAKE_CASE, "_"), equalTo(""));
    }

    @Test
    void testConvertFromCamelCase_ToSnakeCase() {
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SNAKE_CASE, "userName"),
            equalTo("user_name"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SNAKE_CASE, "firstName"),
            equalTo("first_name"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SNAKE_CASE, "id"),
            equalTo("id"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SNAKE_CASE, "userId"),
            equalTo("user_id"));
    }

    @Test
    void testConvertFromCamelCase_ToScreamingSnakeCase() {
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SCREAMING_SNAKE_CASE, "userName"),
            equalTo("USER_NAME"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SCREAMING_SNAKE_CASE, "firstName"),
            equalTo("FIRST_NAME"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SCREAMING_SNAKE_CASE, "id"),
            equalTo("ID"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SCREAMING_SNAKE_CASE, "userId"),
            equalTo("USER_ID"));
    }

    @Test
    void testConvertFromCamelCase_ToKebabCase() {
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.KEBAB_CASE, "userName"),
            equalTo("user-name"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.KEBAB_CASE, "firstName"),
            equalTo("first-name"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.KEBAB_CASE, "id"),
            equalTo("id"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.KEBAB_CASE, "userId"),
            equalTo("user-id"));
    }

    @Test
    void testConvertFromCamelCase_ToCamelCase() {
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.CAMEL_CASE, "userName"),
            equalTo("userName"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.CAMEL_CASE, "firstName"),
            equalTo("firstName"));
    }

    @Test
    void testConvertFromCamelCase_ToPascalCase() {
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.PASCAL_CASE, "userName"),
            equalTo("UserName"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.PASCAL_CASE, "firstName"),
            equalTo("FirstName"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.PASCAL_CASE, "id"),
            equalTo("Id"));
    }

    @Test
    void testConvertToCamelCase_FromPascalCase() {
        assertThat(NamingUtils.convertToCamelCase(TableFieldCaseType.PASCAL_CASE, "UserName"),
            equalTo("userName"));
        assertThat(NamingUtils.convertToCamelCase(TableFieldCaseType.PASCAL_CASE, "FirstName"),
            equalTo("firstName"));
        assertThat(NamingUtils.convertToCamelCase(TableFieldCaseType.PASCAL_CASE, "Id"),
            equalTo("id"));
    }

    @Test
    void testConvertFromPascalCase() {
        assertThat(NamingUtils.convertFromPascalCase(TableFieldCaseType.CAMEL_CASE, "UserName"),
            equalTo("userName"));
        assertThat(NamingUtils.convertFromPascalCase(TableFieldCaseType.CAMEL_CASE, "FirstName"),
            equalTo("firstName"));
        assertThat(NamingUtils.convertFromPascalCase(TableFieldCaseType.CAMEL_CASE, "Id"),
            equalTo("id"));
        assertThat(NamingUtils.convertFromPascalCase(TableFieldCaseType.SNAKE_CASE, "UserName"),
            equalTo("user_name"));
        assertThat(NamingUtils.convertFromPascalCase(TableFieldCaseType.KEBAB_CASE, "FirstName"),
            equalTo("first-name"));
    }

    @Test
    void testConvertToPascalCase() {
        assertThat(NamingUtils.convertToPascalCase(TableFieldCaseType.CAMEL_CASE, "userName"),
            equalTo("UserName"));
        assertThat(NamingUtils.convertToPascalCase(TableFieldCaseType.CAMEL_CASE, "firstName"),
            equalTo("FirstName"));
        assertThat(NamingUtils.convertToPascalCase(TableFieldCaseType.CAMEL_CASE, "id"),
            equalTo("Id"));
        assertThat(NamingUtils.convertToPascalCase(TableFieldCaseType.SNAKE_CASE, "user_name"),
            equalTo("UserName"));
        assertThat(NamingUtils.convertToPascalCase(TableFieldCaseType.KEBAB_CASE, "first-name"),
            equalTo("FirstName"));
    }

    @Test
    void testConvertFromCamelCase_WithAcronyms() {
        // Test consecutive uppercase letters (acronyms)
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SNAKE_CASE, "XMLParser"),
            equalTo("xml_parser"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SNAKE_CASE, "HTTPSConnection"),
            equalTo("https_connection"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SNAKE_CASE, "userID"),
            equalTo("user_id"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SNAKE_CASE, "parseHTML"),
            equalTo("parse_html"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SNAKE_CASE, "IOError"),
            equalTo("io_error"));
        
        // Test with kebab case
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.KEBAB_CASE, "XMLParser"),
            equalTo("xml-parser"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.KEBAB_CASE, "HTTPSConnection"),
            equalTo("https-connection"));
        
        // Test with screaming snake case
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SCREAMING_SNAKE_CASE, "XMLParser"),
            equalTo("XML_PARSER"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SCREAMING_SNAKE_CASE, "HTTPSConnection"),
            equalTo("HTTPS_CONNECTION"));
    }

    @Test
    void testConvertFromCamelCase_EdgeCases() {
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SNAKE_CASE, null), nullValue());
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SNAKE_CASE, ""), equalTo(""));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SNAKE_CASE, "a"), equalTo("a"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SNAKE_CASE, "A"), equalTo("a"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SCREAMING_SNAKE_CASE, "a"), 
            equalTo("A"));
    }

    @Test
    void testConvertFromCamelCase_ComplexCases() {
        // Test mixed scenarios
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SNAKE_CASE, "getHTTPResponseCode"),
            equalTo("get_http_response_code"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SNAKE_CASE, "HTTPResponseCodeXML"),
            equalTo("http_response_code_xml"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SNAKE_CASE, "newCustomerID"),
            equalTo("new_customer_id"));
        assertThat(NamingUtils.convertFromCamelCase(TableFieldCaseType.SNAKE_CASE, "innerStopWatchForDB"),
            equalTo("inner_stop_watch_for_db"));
    }

    @Test
    void testRoundTrip_SnakeCase() {
        // Test converting from camelCase to snake_case and back
        String original = "userName";
        String snakeCase = NamingUtils.convertFromCamelCase(TableFieldCaseType.SNAKE_CASE, original);
        String backToCamel = NamingUtils.convertToCamelCase(TableFieldCaseType.SNAKE_CASE, snakeCase);
        assertThat(backToCamel, equalTo(original));

        original = "firstName";
        snakeCase = NamingUtils.convertFromCamelCase(TableFieldCaseType.SNAKE_CASE, original);
        backToCamel = NamingUtils.convertToCamelCase(TableFieldCaseType.SNAKE_CASE, snakeCase);
        assertThat(backToCamel, equalTo(original));
    }

    @Test
    void testRoundTrip_KebabCase() {
        // Test converting from camelCase to kebab-case and back
        String original = "userName";
        String kebabCase = NamingUtils.convertFromCamelCase(TableFieldCaseType.KEBAB_CASE, original);
        String backToCamel = NamingUtils.convertToCamelCase(TableFieldCaseType.KEBAB_CASE, kebabCase);
        assertThat(backToCamel, equalTo(original));
    }

    @Test
    void testRoundTrip_WithAcronyms() {
        // Note: Acronyms lose their original casing in round-trip
        String original = "XMLParser";
        String snakeCase = NamingUtils.convertFromCamelCase(TableFieldCaseType.SNAKE_CASE, original);
        assertThat(snakeCase, equalTo("xml_parser"));
        String backToCamel = NamingUtils.convertToCamelCase(TableFieldCaseType.SNAKE_CASE, snakeCase);
        assertThat(backToCamel, equalTo("xmlParser")); // lowercase 'xml' instead of 'XML'
    }

    @Test
    void testRoundTrip_PascalCase() {
        String original = "userName";
        String pascalCase = NamingUtils.convertFromCamelCase(TableFieldCaseType.PASCAL_CASE, original);
        String backToCamel = NamingUtils.convertToCamelCase(TableFieldCaseType.PASCAL_CASE, pascalCase);
        assertThat(backToCamel, equalTo(original));

        original = "firstName";
        pascalCase = NamingUtils.convertFromCamelCase(TableFieldCaseType.PASCAL_CASE, original);
        backToCamel = NamingUtils.convertToCamelCase(TableFieldCaseType.PASCAL_CASE, pascalCase);
        assertThat(backToCamel, equalTo(original));
    }
}