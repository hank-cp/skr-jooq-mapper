package org.laxture.skr.jooq.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.UpdatableRecordImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.laxture.skr.jooq.mapper.SkrRecordUnmapperProvider;
import org.laxture.skr.jooq.mapper.TableFieldCaseType;
import org.laxture.skr.jooq.mapper.converter.ConverterRegistry;
import org.laxture.skr.jooq.mapper.converter.json.JsonArrayConverter;
import org.laxture.skr.jooq.mapper.converter.json.JsonObject2MapConverter;
import org.laxture.skr.jooq.mapper.converter.json.JsonObjectConverter;
import org.laxture.skr.jooq.mapper.misc.ObjectMapperConfigurer;
import org.laxture.skr.jooq.test.model.Address;
import org.laxture.skr.jooq.test.model.EducationExperience;
import org.laxture.skr.jooq.test.model.User;
import org.laxture.skr.jooq.test.model.UserProfile;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SkrRecordUnmapperTest {

    private DSLContext dsl;
    private Connection connection;
    private ObjectMapper objectMapper;
    private Table<?> userTable;

    @BeforeEach
    void setup() throws Exception {
        objectMapper = ObjectMapperConfigurer.setupPersistentObjectMapper(new ObjectMapper());

        ConverterRegistry converterRegistry = new ConverterRegistry();
        converterRegistry.registerConverter(new JsonObjectConverter(objectMapper), null);
        converterRegistry.registerConverter(new JsonArrayConverter(objectMapper), null);
        converterRegistry.registerConverter(new JsonObject2MapConverter(objectMapper), null);

        connection = DriverManager.getConnection("jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1", "sa", "");

        Configuration configuration = new org.jooq.impl.DefaultConfiguration();
        configuration.set(SQLDialect.H2);
        configuration.set(connection);
        configuration.set(new SkrRecordUnmapperProvider(() -> dsl,
            converterRegistry, TableFieldCaseType.SCREAMING_SNAKE_CASE, objectMapper));

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
                note VARCHAR(200), -- leftover field
                misc_info JSON, -- leftover field,
                immutable TEXT -- transient field
            );
            """);

        dsl.execute("""
            INSERT INTO users (name, age, created_at, birth_date, 
                               address_line1, address_line2, address_city, 
                               user_profile_avatar_url,
                               edu_experiences, recent_edu_experience,
                               meta_info, friend_ids, note, misc_info, immutable)
            VALUES ('Skr', 30, '2023-01-01 12:00:00', '2000-01-01', 
                    '123 Main St', 'Apt 4B', 'New York', 
                    'https://avatar.com/hank.jpg', 
                    JSON '[{"institute":"MIT","major":"CS"},{"institute":"MIT","major":"Math"}]', 
                    JSON '{"institute":"MIT","major":"CS"}', 
                    JSON '{"habit":"reading", "married":true}', ARRAY[1,2,3], 'note', JSON '{"a":"b"}', 'immutable');
            """);

        userTable = dsl.meta()
            .filterSchemas(s -> s.getName().equals("PUBLIC"))
            .getTables().stream()
            .filter(t -> t.getName().equals("USERS"))
            .findAny().orElse(null);
    }

    @Test
    void testToRecord() {
        User user = new User();
        user.setId(10);
        user.setName("Skr");
        user.setAge(30);
        user.setCreatedAt(LocalDateTime.parse("2023-01-01T12:00:00"));
        user.setBirthDate(LocalDate.parse("2000-01-01"));
        user.setAddress(new Address());
        user.getAddress().setLine1("123 Main St");
        user.getAddress().setLine2("Apt 4B");
        user.getAddress().setCity("New York");
        user.setUserProfile(new UserProfile());
        user.getUserProfile().setAvatarUrl("https://avatar.com/hank.jpg");
        user.setEduExperiences(List.of(
            EducationExperience.of("MIT", "CS", "transient"),
            EducationExperience.of("MIT", "Math", "transient")));
        user.setRecentEduExperience(EducationExperience.of("MIT", "CS", "transient"));
        user.setMetaInfo(Map.of("married", true));
        user.setFriendIds(List.of(1L, 2L, 3L));
        user.setExtras(new LinkedHashMap<>());
        user.getExtras().put("note", "note");
        user.getExtras().put("miscInfo", Map.of("a", "b"));
        user.setImmutable("immutable");

        UpdatableRecord<?> record = new UpdatableRecordImpl(userTable);
        record.attach(dsl.configuration());
        record.from(user);
        assertThat(record, notNullValue());
        assertThat(record.getValue(userTable.field("ID")), is(10L));
        assertThat(record.getValue(userTable.field("NAME")), is("Skr"));
        assertThat(record.getValue(userTable.field("AGE")), is(30));
        assertThat(record.getValue(userTable.field("CREATED_AT")), is(Timestamp.valueOf(user.getCreatedAt())));
        assertThat(record.getValue(userTable.field("BIRTH_DATE")), is(Date.valueOf(user.getBirthDate())));
        assertThat(record.getValue(userTable.field("ADDRESS_LINE1")), is("123 Main St"));
        assertThat(record.getValue(userTable.field("ADDRESS_LINE2")), is("Apt 4B"));
        assertThat(record.getValue(userTable.field("ADDRESS_CITY")), is("New York"));
        assertThat(record.getValue(userTable.field("USER_PROFILE_AVATAR_URL")), is("https://avatar.com/hank.jpg"));
        assertThat(record.getValue(userTable.field("EDU_EXPERIENCES")), equalTo(JSON.json("""
            [{"institute":"MIT","major":"CS"},{"institute":"MIT","major":"Math"}]""")));
        assertThat(record.getValue(userTable.field("RECENT_EDU_EXPERIENCE")), equalTo(JSON.json("""
            {"institute":"MIT","major":"CS"}""")));
        assertThat(record.getValue(userTable.field("META_INFO")), equalTo(JSON.json("""
            {"married":true}""")));
        assertThat(record.getValue(userTable.field("FRIEND_IDS")), notNullValue());
        assertThat(record.getValue(userTable.field("FRIEND_IDS")), equalTo(new Long[]{1L, 2L, 3L}));
        assertThat(record.getValue(userTable.field("NOTE")), is("note"));
        assertThat(record.getValue(userTable.field("MISC_INFO")), is(JSON.json("""
            {"a":"b"}""")));
        assertThat(record.getValue(userTable.field("IMMUTABLE")), nullValue());
    }

    @Test
    void testUpdate() {
        org.jooq.Record record = dsl.resultQuery("SELECT * FROM users WHERE name = 'Skr'").fetchOne();
        User user = record.into(User.class);
        user.setAge(31);
        user.setImmutable("changed");
        record.from(user);

        assertThat(record.getValue(userTable.field("ID")), is(1L));
        assertThat(record.getValue(userTable.field("NAME")), is("Skr"));
        assertThat(record.getValue(userTable.field("AGE")), is(31));
        assertThat(record.getValue(userTable.field("CREATED_AT")), is(Timestamp.valueOf(user.getCreatedAt())));
        assertThat(record.getValue(userTable.field("BIRTH_DATE")), is(Date.valueOf(user.getBirthDate())));
        assertThat(record.getValue(userTable.field("ADDRESS_LINE1")), is("123 Main St"));
        assertThat(record.getValue(userTable.field("ADDRESS_LINE2")), is("Apt 4B"));
        assertThat(record.getValue(userTable.field("ADDRESS_CITY")), is("New York"));
        assertThat(record.getValue(userTable.field("USER_PROFILE_AVATAR_URL")), is("https://avatar.com/hank.jpg"));
        assertThat(record.getValue(userTable.field("EDU_EXPERIENCES")), equalTo(JSON.json("""
            [{"institute":"MIT","major":"CS"},{"institute":"MIT","major":"Math"}]""")));
        assertThat(record.getValue(userTable.field("RECENT_EDU_EXPERIENCE")), equalTo(JSON.json("""
            {"institute":"MIT","major":"CS"}""")));
        assertThat(record.getValue(userTable.field("META_INFO")), equalTo(JSON.json("""
            {"habit":"reading","married":true}""")));
        assertThat(record.getValue(userTable.field("FRIEND_IDS")), notNullValue());
        assertThat(record.getValue(userTable.field("FRIEND_IDS")), equalTo(new Long[]{1L, 2L, 3L}));
        assertThat(record.getValue(userTable.field("NOTE")), is("note"));
        assertThat(record.getValue(userTable.field("MISC_INFO")), is(JSON.json("""
            {"a":"b"}""")));
        assertThat(record.getValue(userTable.field("IMMUTABLE")), equalTo("immutable"));
    }
}
