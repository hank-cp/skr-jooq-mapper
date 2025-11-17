package org.laxture.skr.jooq.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.laxture.skr.jooq.mapper.SkrRecordMapperProvider;
import org.laxture.skr.jooq.mapper.TableFieldCaseType;
import org.laxture.skr.jooq.mapper.converter.ConverterRegistry;
import org.laxture.skr.jooq.mapper.converter.json.JsonArrayConverter;
import org.laxture.skr.jooq.mapper.converter.json.JsonObject2MapConverter;
import org.laxture.skr.jooq.mapper.converter.json.JsonObjectConverter;
import org.laxture.skr.jooq.mapper.misc.ObjectMapperConfigurer;
import org.laxture.skr.jooq.test.model.EducationExperience;
import org.laxture.skr.jooq.test.model.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SkrRecordMapperTest {

    private DSLContext dsl;
    private Connection connection;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws Exception {
        objectMapper = ObjectMapperConfigurer.setupPersistentObjectMapper(new ObjectMapper());

        ConverterRegistry converterRegistry = new ConverterRegistry();
        converterRegistry.registerConverter(new JsonObjectConverter<>(objectMapper, EducationExperience.class), null);
        converterRegistry.registerConverter(new JsonArrayConverter<>(objectMapper, EducationExperience.class), null);
        converterRegistry.registerConverter(new JsonObject2MapConverter(objectMapper), null);

        connection = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "");

        Configuration configuration = new org.jooq.impl.DefaultConfiguration();
        configuration.set(SQLDialect.H2);
        configuration.set(connection);
        configuration.set(new SkrRecordMapperProvider(converterRegistry, TableFieldCaseType.SCREAMING_SNAKE_CASE));

        dsl = DSL.using(configuration);

        dsl.execute("DROP TABLE IF EXISTS users");
        dsl.execute("""
            CREATE TABLE users (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100),
                age INT,
                created_at TIMESTAMP,
                birth_date DATE,
                address_id BIGINT,
                address_line1 VARCHAR(200),
                address_line2 VARCHAR(200),
                address_city VARCHAR(100),
                user_profile_avatar_url VARCHAR(100),
                edu_experiences JSON,
                recent_edu_experience JSON,
                meta_info JSON,
                friend_ids BIGINT ARRAY,
                note VARCHAR(200),
                misc_info JSON);
            """);

        dsl.execute("""
            INSERT INTO users (name, age, created_at, birth_date, 
                               address_line1, address_line2, address_city, 
                               user_profile_avatar_url,
                               edu_experiences, recent_edu_experience,
                               meta_info, friend_ids, note, misc_info)
            VALUES ('Skr', 30, '2023-01-01 12:00:00', '2000-01-01', 
                    '123 Main St', 'Apt 4B', 'New York', 
                    'https://avatar.com/hank.jpg', 
                    JSON '[{"institute":"MIT","major":"CS"},{"institute":"MIT","major":"Math"}]', 
                    JSON '{"institute":"MIT","major":"CS"}', 
                    JSON '{"habit":"reading", "married":true}', ARRAY[1,2,3], 'note', '{}');
        """);
    }

    @Test
    void testBasicMapping() {
        org.jooq.Record record = dsl.resultQuery("SELECT * FROM users WHERE name = 'Skr'").fetchOne();

        User user = record.into(User.class);

        assertThat(user, notNullValue());
        assertThat(user.getName(), is("Skr"));
        assertThat(user.getAge(), is(30));
        assertThat(user.getCreatedAt(), is(LocalDateTime.parse("2023-01-01 12:00:00")));
        assertThat(user.getBirthDate(), is(LocalDate.parse("2000-01-01")));
    }
//
//    @Test
//    void testCascadeMapping() {
//        org.jooq.Record record = dsl.resultQuery(
//                "SELECT name, age, address_line1, address_line2, address_city FROM users WHERE name = 'Hank'"
//        ).fetchOne();
//
//        User user = record.into(User.class);
//
//        assertNotNull(user);
//        assertEquals("Hank", user.getName());
//        assertEquals(30, user.getAge());
//        assertNotNull(user.getAddress());
//        assertEquals("123 Main St", user.getAddress().getLine1());
//        assertEquals("Apt 4B", user.getAddress().getLine2());
//        assertEquals("New York", user.getAddress().getCity());
//    }
//
//    @Test
//    void testLeftoverCollector() {
//        org.jooq.Record record = dsl.resultQuery(
//                "SELECT name, age, extra_field FROM users WHERE name = 'Hank'"
//        ).fetchOne();
//
//        User user = record.into(User.class);
//
//        assertNotNull(user);
//        assertNotNull(user.getExtras());
//        assertTrue(user.getExtras().containsKey("extra_field"));
//        assertEquals("extra_value", user.getExtras().get("extra_field"));
//    }
//
//    @Test
//    void testPostMappingHook() {
//        org.jooq.Record record = dsl.resultQuery("SELECT name, age FROM users WHERE name = 'Hank'").fetchOne();
//
//        User user = record.into(User.class);
//
//        assertNotNull(user);
//        assertTrue(user.isPostMappingCalled());
//    }
//
//    @Test
//    void testPrimaryKeyNullHandling() {
//        dsl.execute("INSERT INTO users (name, age) VALUES ('John', 25)");
//
//        org.jooq.Record record = dsl.resultQuery(
//                "SELECT name, age, address_line1, address_line2, address_city FROM users WHERE name = 'John'"
//        ).fetchOne();
//
//        User user = record.into(User.class);
//
//        assertNotNull(user);
//        assertEquals("John", user.getName());
//        assertEquals(25, user.getAge());
//        assertNull(user.getAddress());
//    }
}
