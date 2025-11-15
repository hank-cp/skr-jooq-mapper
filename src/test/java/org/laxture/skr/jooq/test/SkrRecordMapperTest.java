package org.laxture.skr.jooq.test;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.laxture.skr.jooq.mapper.SkrRecordMapperProvider;
import org.laxture.skr.jooq.test.model.Address;
import org.laxture.skr.jooq.test.model.User;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

class SkrRecordMapperTest {

    private DSLContext dsl;
    private Connection connection;

    @BeforeEach
    void setup() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "");
        
        Configuration configuration = new org.jooq.impl.DefaultConfiguration();
        configuration.set(SQLDialect.H2);
        configuration.set(connection);
        configuration.set(new SkrRecordMapperProvider());
        
        dsl = DSL.using(configuration);

        dsl.execute("DROP TABLE IF EXISTS users");
        dsl.execute("CREATE TABLE users (" +
                "name VARCHAR(100), " +
                "age INT, " +
                "address_line1 VARCHAR(200), " +
                "address_line2 VARCHAR(200), " +
                "address_city VARCHAR(100), " +
                "extra_field VARCHAR(100))");

        dsl.execute("INSERT INTO users (name, age, address_line1, address_line2, address_city, extra_field) " +
                "VALUES ('Hank', 30, '123 Main St', 'Apt 4B', 'New York', 'extra_value')");
    }

    @Test
    void testBasicMapping() {
        org.jooq.Record record = dsl.resultQuery("SELECT name, age FROM users WHERE name = 'Hank'").fetchOne();
        
        User user = record.into(User.class);
        
        assertNotNull(user);
        assertEquals("Hank", user.getName());
        assertEquals(30, user.getAge());
    }

    @Test
    void testCascadeMapping() {
        org.jooq.Record record = dsl.resultQuery(
                "SELECT name, age, address_line1, address_line2, address_city FROM users WHERE name = 'Hank'"
        ).fetchOne();
        
        User user = record.into(User.class);
        
        assertNotNull(user);
        assertEquals("Hank", user.getName());
        assertEquals(30, user.getAge());
        assertNotNull(user.getAddress());
        assertEquals("123 Main St", user.getAddress().getLine1());
        assertEquals("Apt 4B", user.getAddress().getLine2());
        assertEquals("New York", user.getAddress().getCity());
    }

    @Test
    void testLeftoverCollector() {
        org.jooq.Record record = dsl.resultQuery(
                "SELECT name, age, extra_field FROM users WHERE name = 'Hank'"
        ).fetchOne();
        
        User user = record.into(User.class);
        
        assertNotNull(user);
        assertNotNull(user.getExtras());
        assertTrue(user.getExtras().containsKey("extra_field"));
        assertEquals("extra_value", user.getExtras().get("extra_field"));
    }

    @Test
    void testPostMappingHook() {
        org.jooq.Record record = dsl.resultQuery("SELECT name, age FROM users WHERE name = 'Hank'").fetchOne();
        
        User user = record.into(User.class);
        
        assertNotNull(user);
        assertTrue(user.isPostMappingCalled());
    }

    @Test
    void testPrimaryKeyNullHandling() {
        dsl.execute("INSERT INTO users (name, age) VALUES ('John', 25)");
        
        org.jooq.Record record = dsl.resultQuery(
                "SELECT name, age, address_line1, address_line2, address_city FROM users WHERE name = 'John'"
        ).fetchOne();
        
        User user = record.into(User.class);
        
        assertNotNull(user);
        assertEquals("John", user.getName());
        assertEquals(25, user.getAge());
        assertNull(user.getAddress());
    }
}
