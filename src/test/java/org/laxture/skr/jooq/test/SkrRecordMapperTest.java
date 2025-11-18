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
import org.laxture.skr.jooq.test.model.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

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
        converterRegistry.registerConverter(new JsonObjectConverter(objectMapper), null);
        converterRegistry.registerConverter(new JsonArrayConverter(objectMapper), null);
        converterRegistry.registerConverter(new JsonObject2MapConverter(objectMapper), null);

        connection = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "");

        Configuration configuration = new org.jooq.impl.DefaultConfiguration();
        configuration.set(SQLDialect.H2);
        configuration.set(connection);
        configuration.set(new SkrRecordMapperProvider(converterRegistry,
            TableFieldCaseType.SCREAMING_SNAKE_CASE, objectMapper));

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
                meta_info JSON,  -- leftover field
                friend_ids BIGINT ARRAY,
                note VARCHAR(200), -- leftover field
                misc_info JSON -- leftover field
            );
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
                    JSON '{"habit":"reading", "married":true}', ARRAY[1,2,3], 'note', JSON '{"a":"b"}');
            """);
    }

    @Test
    void testMapping() {
        org.jooq.Record record = dsl.resultQuery("SELECT * FROM users WHERE name = 'Skr'").fetchOne();

        User user = record.into(User.class);

        assertThat(user, notNullValue());
        assertThat(user.getName(), is("Skr"));
        assertThat(user.getAge(), is(30));
        assertThat(user.getCreatedAt(), is(LocalDateTime.parse("2023-01-01T12:00:00")));
        assertThat(user.getBirthDate(), is(LocalDate.parse("2000-01-01")));
        assertThat(user.getAddress(), notNullValue());
        assertThat(user.getAddress().getLine1(), is("123 Main St"));
        assertThat(user.getAddress().getLine2(), is("Apt 4B"));
        assertThat(user.getAddress().getCity(), is("New York"));
        assertThat(user.getUserProfile(), notNullValue());
        assertThat(user.getUserProfile().getAvatarUrl(), is("https://avatar.com/hank.jpg"));
        assertThat(user.getEduExperiences(), notNullValue());
        assertThat(user.getEduExperiences().size(), is(2));
        assertThat(user.getEduExperiences().get(0).institute, is("MIT"));
        assertThat(user.getEduExperiences().get(0).major, is("CS"));
        assertThat(user.getEduExperiences().get(1).institute, is("MIT"));
        assertThat(user.getEduExperiences().get(1).major, is("Math"));
        assertThat(user.getRecentEduExperience(), notNullValue());
        assertThat(user.getRecentEduExperience().institute, is("MIT"));
        assertThat(user.getRecentEduExperience().major, is("CS"));
        assertThat(user.getMetaInfo(), notNullValue());
        assertThat(user.getMetaInfo().get("habit"), is("reading"));
        assertThat(user.getMetaInfo().get("married"), is(true));
        assertThat(user.getFriendIds(), notNullValue());
        assertThat(user.getFriendIds().size(), is(3));
        assertThat(user.getFriendIds().get(0), is(1L));
        assertThat(user.getFriendIds().get(1), is(2L));
        assertThat(user.getFriendIds().get(2), is(3L));
        assertThat(user.getExtras(), notNullValue());
        assertThat(user.getExtras(), aMapWithSize(2));

        // verify leftover fields
        assertThat(user.getExtras().get("note"), is("note"));
        Map<String, Object> miscInfo = (Map<String, Object>) user.getExtras().get("miscInfo");
        assertThat(miscInfo, notNullValue());
        assertThat(miscInfo.get("a"), is("b"));
    }
}
