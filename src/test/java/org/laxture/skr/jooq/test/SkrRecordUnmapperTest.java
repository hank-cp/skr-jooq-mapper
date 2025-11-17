package org.laxture.skr.jooq.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.laxture.skr.jooq.mapper.SkrRecordUnmapperProvider;
import org.laxture.skr.jooq.mapper.TableFieldCaseType;
import org.laxture.skr.jooq.mapper.converter.ConverterRegistry;
import org.laxture.skr.jooq.mapper.converter.json.JsonArrayConverter;
import org.laxture.skr.jooq.mapper.converter.json.JsonObjectConverter;
import org.laxture.skr.jooq.mapper.misc.ObjectMapperConfigurer;
import org.laxture.skr.jooq.test.model.Address;
import org.laxture.skr.jooq.test.model.EducationExperience;
import org.laxture.skr.jooq.test.model.User;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

class SkrRecordUnmapperTest {

    private DSLContext dsl;
    private Connection connection;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws Exception {
        objectMapper = ObjectMapperConfigurer.setupPersistentObjectMapper(new ObjectMapper());

        ConverterRegistry converterRegistry = new ConverterRegistry();
        converterRegistry.registerConverter(new JsonObjectConverter<EducationExperience>(objectMapper), null);
        converterRegistry.registerConverter(new JsonArrayConverter<EducationExperience>(objectMapper), null);

        connection = DriverManager.getConnection("jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1", "sa", "");

        Configuration configuration = new org.jooq.impl.DefaultConfiguration();
        configuration.set(SQLDialect.H2);
        configuration.set(connection);
        configuration.set(new SkrRecordUnmapperProvider(() -> dsl,
            converterRegistry, TableFieldCaseType.CAMEL_CASE));

        dsl = DSL.using(configuration);
    }

//    @Test
//    void testBasicUnmapping() {
//        User user = new User();
//        user.setName("Alice");
//        user.setAge(28);
//
//        assertNotNull(user);
//    }
//
//    @Test
//    void testCascadeUnmapping() {
//        User user = new User();
//        user.setName("Bob");
//        user.setAge(35);
//
//        Address address = new Address();
//        address.setLine1("456 Elm St");
//        address.setLine2("Suite 100");
//        address.setCity("Boston");
//        user.setAddress(address);
//
//        SkrRecordUnmapperProvider provider = new SkrRecordUnmapperProvider();
//
//        assertNotNull(user);
//    }
//
//    @Test
//    void testNullHandling() {
//        SkrRecordUnmapperProvider provider = new SkrRecordUnmapperProvider();
//
//        assertNotNull(provider);
//    }
}
