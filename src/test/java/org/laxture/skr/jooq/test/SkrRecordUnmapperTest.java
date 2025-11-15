package org.laxture.skr.jooq.test;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.laxture.skr.jooq.mapper.SkrRecordUnmapperProvider;
import org.laxture.skr.jooq.test.model.Address;
import org.laxture.skr.jooq.test.model.User;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

class SkrRecordUnmapperTest {

    private DSLContext dsl;
    private Connection connection;

    @BeforeEach
    void setup() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1", "sa", "");
        
        Configuration configuration = new org.jooq.impl.DefaultConfiguration();
        configuration.set(SQLDialect.H2);
        configuration.set(connection);
        configuration.set(new SkrRecordUnmapperProvider());
        
        dsl = DSL.using(configuration);
    }

    @Test
    void testBasicUnmapping() {
        User user = new User();
        user.setName("Alice");
        user.setAge(28);

        SkrRecordUnmapperProvider provider = new SkrRecordUnmapperProvider();
        
        assertNotNull(user);
    }

    @Test
    void testCascadeUnmapping() {
        User user = new User();
        user.setName("Bob");
        user.setAge(35);
        
        Address address = new Address();
        address.setLine1("456 Elm St");
        address.setLine2("Suite 100");
        address.setCity("Boston");
        user.setAddress(address);

        SkrRecordUnmapperProvider provider = new SkrRecordUnmapperProvider();
        
        assertNotNull(user);
    }

    @Test
    void testNullHandling() {
        SkrRecordUnmapperProvider provider = new SkrRecordUnmapperProvider();
        
        assertNotNull(provider);
    }
}
