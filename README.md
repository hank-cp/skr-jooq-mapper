[![GitHub release](https://img.shields.io/github/release/hank-cp/skr-jooq-mapper.svg)](https://github.com/hank-cp/skr-jooq-mapper/releases)
![Maven Central](https://img.shields.io/maven-central/v/org.laxture.skr-jooq-mapper/skr-jooq-mapper.svg)
![Test](https://github.com/hank-cp/skr-jooq-mapper/workflows/CI%20Test/badge.svg)
![GitHub](https://img.shields.io/github/license/hank-cp/skr-jooq-mapper)
![GitHub last commit](https://img.shields.io/github/last-commit/hank-cp/skr-jooq-mapper.svg)

# skr-jooq-mapper

A lightweight, extensible ORM framework built on top of [jOOQ](https://www.jooq.org/) that provides bi-directional mapping between jOOQ Records and Java POJOs.

[中文](README.zh-cn.md)

## Introduction

`skr-jooq-mapper` bridges the gap between jOOQ's powerful SQL capabilities and object-oriented domain models. While jOOQ excels at type-safe SQL operations, it lacks flexible mapping capabilities for complex object structures. This library fills that gap by providing:

- **Runtime-extensible type conversion system** - Register and unregister custom converters dynamically
- **First-class JSON support** - Native handling of JSON/JSONB database types with Jackson integration  
- **Cascade mapping** - Automatic mapping of nested objects using field name prefixes
- **Spring Boot auto-configuration** - Zero-configuration setup in Spring Boot applications

Choose `skr-jooq-mapper` when you want the SQL power of jOOQ combined with the flexibility of a modern ORM for object mapping.

## Key Features

### Runtime Dynamic Custom Mapping

Unlike compile-time converters, `skr-jooq-mapper` allows you to register and unregister type converters at runtime:

- **Priority-based matching** - Multiple converters can target the same types; the highest priority wins
- **Built-in converter override** - Custom converters with priority > 10 override built-in ones
- **Performance optimized** - Converter matching results are cached for fast lookups
- **Lifecycle management** - Register converters with keys for easy batch removal

```java
// Register a custom converter
converterRegistry.registerConverter(new MyCustomConverter(), "my-app");

// Later, unregister all converters from this module
converterRegistry.unregisterConverter("my-app");
```

### Excellent JSON Support

Native support for jOOQ's JSON and JSONB types with seamless Jackson integration:

- **Automatic JSON serialization/deserialization** - POJOs, Maps, Lists, and JsonNode
- **Type-safe mapping** - JSON arrays to `List<T>`, JSON objects to POJOs or `Map<String, Object>`
- **Persistence-aware serialization** - Respects `@JsonTransient` and other transient annotations
- **Flexible JSON handling** - Handle JSON as objects, maps, or raw strings

```java
public class User {
    private List<Education> eduExperiences;  // Maps from JSON array
    private Map<String, Object> metadata;     // Maps from JSON object
    private JsonNode settings;                // Raw JSON node
}
```

### Comparison with Other jOOQ Mapper Libraries

| Feature                 | jOOQ Default Mapper | ModelMapper    | SimpleFlatMapper | skr-jooq-mapper       |
|-------------------------|---------------------|----------------|------------------|-----------------------|
| Custom Type Conversion  | ❌ Limited           | ✅ Compile-time | ✅ Compile-time   | ✅ Runtime dynamic     |
| JSON/JSONB Support      | ❌ Basic             | ❌ None         | ❌ None           | ✅ Excellent (Jackson) |
| Nested Object Mapping   | ❌ None              | ✅ Support      | ✅ Support        | ✅ Support             |
| Runtime Extensibility   | ❌ None              | ❌ None         | ❌ None           | ✅ Full support        |
| Performance             | ⚡ Fast              | ⚡ Fast         | ⚡ Fast           | ⚡ Fast (cached)       |
| Spring Boot Integration | ❌ Manual            | ❌ Manual       | ❌ Manual         | ✅ Auto-configuration  |

### Why jOOQ + Mapper over JPA?

While JPA (Java Persistence API) is a popular ORM standard, it comes with significant limitations that make it less suitable for modern, database-centric applications:

#### JPA Limitations

- **Heavy and hard to control** - JPA implementations like Hibernate are complex frameworks with steep learning curves and opaque behavior
- **Static configuration** - Impossible to change entity mappings, converters, or persistence strategies at runtime
- **Thick abstraction layer** - The ORM abstraction hides SQL, making it difficult to:
  - Understand what queries are actually being executed
  - Optimize complex queries or joins
  - Debug performance issues
  - Control database-specific behavior
- **Limited database feature utilization** - JPA's database-agnostic approach prevents you from leveraging modern database capabilities:
  - **PostgreSQL**: JSONB operators, array functions, CTEs, window functions, full-text search
  - **MySQL/MariaDB**: JSON functions, spatial data types
  - **Oracle**: Advanced analytics, hierarchical queries
- **N+1 query problems** - Easy to accidentally trigger performance-killing query patterns
- **Lazy loading issues** - Session management complexity, especially in web applications

#### jOOQ + skr-jooq-mapper Advantages

| Aspect | JPA | jOOQ + skr-jooq-mapper |
|--------|-----|------------------------|
| **Control** | ❌ Opaque SQL generation | ✅ Full SQL control with type-safe DSL |
| **Runtime Flexibility** | ❌ Static entity mappings | ✅ Dynamic converter registration |
| **Database Features** | ❌ Lowest common denominator | ✅ Database-specific optimizations |
| **Performance Tuning** | ❌ Query hints, complex joins | ✅ Direct SQL optimization |
| **Learning Curve** | ❌ High (HQL, JPQL, lifecycle) | ✅ Lower (just SQL + Java) |
| **Type Safety** | ✅ Compile-time (entities) | ✅ Compile-time (SQL + mapping) |
| **Modern DB Types** | ❌ Limited (requires custom types) | ✅ First-class JSON, arrays, custom types |

#### When to Choose jOOQ + Mapper

Choose jOOQ with skr-jooq-mapper when you need:

- 🎯 **Full SQL control** - Complex queries, analytics, reporting
- ⚡ **Maximum performance** - Every query matters
- 🔧 **Database-specific features** - JSONB, arrays, window functions, CTEs
- 🚀 **Runtime flexibility** - Multi-tenant apps, dynamic schemas
- 📊 **Data-centric applications** - More reads than writes, complex aggregations

### Why jOOQ + Mapper over MyBatis?

MyBatis is a popular SQL mapping framework, but it lacks modern type safety and productivity features:

| Feature | MyBatis | jOOQ + skr-jooq-mapper |
|---------|---------|------------------------|
| **Type Safety** | ❌ No compile-time SQL checking | ✅ Type-safe SQL DSL |
| **SQL Writing** | ❌ XML/annotations, error-prone | ✅ Java DSL, IDE-friendly |
| **Compile-time Checking** | ❌ Runtime errors on SQL mistakes | ✅ Compilation fails on schema changes |
| **Result Mapping** | ❌ Manual XML/annotation mapping | ✅ Automatic bidirectional mapping |
| **Type Conversion** | ❌ Limited, requires custom handlers | ✅ Extensible runtime converters |
| **Refactoring** | ❌ XML strings break on renames | ✅ IDE refactoring support |
| **JSON Support** | ❌ Manual serialization | ✅ Native JSON/JSONB mapping |
| **Runtime Extensibility** | ❌ Static configuration | ✅ Dynamic converter registration |
| **Learning Curve** | ✅ Easy (just SQL) | ⚖️ Moderate (SQL + DSL) |

#### MyBatis Pain Points

- **No compile-time safety** - SQL typos, wrong column names, type mismatches only discovered at runtime
- **XML maintenance burden** - SQL scattered across XML files, hard to navigate and refactor
- **Manual result mapping** - Tedious `<resultMap>` configurations for complex objects
- **Weak type conversion** - Custom `TypeHandler` for every non-standard type
- **Poor IDE support** - No auto-completion, navigation, or refactoring for SQL in XML
- **Schema evolution friction** - Database changes break at runtime, not compile-time

#### jOOQ + skr-jooq-mapper Advantages

```java
// MyBatis - Runtime errors possible
@Select("SELECT user_name FROM users WHERE id = #{id}") // Typo: should be username
User findUser(Long id);

// jOOQ - Compile-time safety
User user = dsl.select(USERS.USERNAME) // Compile error if column doesn't exist
    .from(USERS)
    .where(USERS.ID.eq(id))
    .fetchOne()
    .into(User.class); // Automatic type-safe mapping
```

**Key Benefits:**

- 🛡️ **Type safety** - Database schema changes cause compilation errors, not production bugs
- 🔄 **Automatic mapping** - No manual `<resultMap>` or `TypeHandler` for most cases
- 📝 **Better tooling** - Full IDE support: auto-completion, refactoring, navigation
- 🎨 **Cleaner code** - SQL and Java together, no XML context switching
- ⚙️ **Runtime flexibility** - Register custom converters dynamically
- 🚀 **Modern types** - First-class JSON, temporal types, custom converters

#### When to Choose jOOQ + Mapper

Choose jOOQ with skr-jooq-mapper over MyBatis when:

- ✅ You want **compile-time safety** and early error detection
- ✅ Your database schema **changes frequently**
- ✅ You need **complex type conversions** (JSON, custom types)
- ✅ You value **IDE support** and refactoring capabilities
- ✅ You prefer **SQL in Java** over XML/annotations

Stick with MyBatis if:

- ⚠️ You have legacy XML mappings and no budget for migration
- ⚠️ Your team strongly prefers SQL in separate files
- ⚠️ You only use simple POJO mappings with basic types

### Other Features

#### Cascade Mapping
Map nested objects using field name separators (default: `_`):

```java
public class User {
    private String name;
    private Address address;  // Maps from address_* fields
}

public class Address {
    private String line1;     // Maps from address_line1
    private String city;      // Maps from address_city
}
```

#### Field Naming Convention Support
Automatic conversion between database and Java naming styles:
- `SNAKE_CASE` - `user_name` (default)
- `CAMEL_CASE` - `userName`  
- `SCREAMING_SNAKE_CASE` - `USER_NAME`
- `KEBAB_CASE` - `user-name`

#### Rich Annotation Support
Fine-grained control over mapping behavior:

- **`@Transient`** - Exclude field from persistence and loading
- **`@JsonTransient`** - Exclude from JSON serialization only
- **`@LeftoverCollector`** - Collect unmapped columns into a `Map<String, Object>`
- **`@MappingInstantiator`** - Designate constructor/builder for object creation
- **`@PrimaryKey`** - Mark primary key fields (null PK = null nested object)
- **`@Immutable`** - Mark fields as read-only (skip on updates)

## Getting Started

### Installation

#### Maven
```xml
<dependency>
    <groupId>org.laxture.skr-jooq-mapper</groupId>
    <artifactId>skr-jooq-mapper</artifactId>
    <version>0.0.1</version>
</dependency>
```

#### Gradle
```gradle
implementation 'org.laxture.skr-jooq-mapper:skr-jooq-mapper:0.0.1'
```

### Pure Java Environment

Complete example of setting up `skr-jooq-mapper` in a non-Spring application:

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.laxture.skr.jooq.mapper.*;
import org.laxture.skr.jooq.mapper.converter.ConverterRegistry;
import org.laxture.skr.jooq.mapper.converter.json.*;
import org.laxture.skr.jooq.mapper.misc.ObjectMapperConfigurer;

import java.sql.Connection;
import java.sql.DriverManager;

public class Example {
    public static void main(String[] args) throws Exception {
        // 1. Define your model
        class User {
            private Long id;
            private String name;
            private int age;
            
            // Getters and setters...
        }
        
        // 2. Create ConverterRegistry and register converters
        ObjectMapper objectMapper = ObjectMapperConfigurer
            .setupPersistentObjectMapper(new ObjectMapper());
        
        ConverterRegistry converterRegistry = new ConverterRegistry();
        converterRegistry.registerConverter(new JsonObjectConverter(objectMapper), null);
        converterRegistry.registerConverter(new JsonArrayConverter(objectMapper), null);
        converterRegistry.registerConverter(new JsonObject2MapConverter(objectMapper), null);
        converterRegistry.registerConverter(new JsonbObjectConverter(objectMapper), null);
        converterRegistry.registerConverter(new JsonbArrayConverter(objectMapper), null);
        converterRegistry.registerConverter(new JsonbObject2MapConverter(objectMapper), null);
        
        // 3. Create mapper and unmapper providers
        SkrRecordMapperProvider mapperProvider = new SkrRecordMapperProvider(
            converterRegistry,
            TableFieldCaseType.SNAKE_CASE,
            objectMapper
        );
        
        DSLContextProvider dslContextProvider = () -> dslContext; // Your DSLContext instance
        SkrRecordUnmapperProvider unmapperProvider = new SkrRecordUnmapperProvider(
            dslContextProvider,
            converterRegistry,
            TableFieldCaseType.SNAKE_CASE,
            objectMapper
        );
        
        // 4. Configure jOOQ with the providers
        Connection connection = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");
        Configuration configuration = new org.jooq.impl.DefaultConfiguration()
            .set(SQLDialect.H2)
            .set(connection)
            .set(mapperProvider)
            .set(unmapperProvider);
        
        DSLContext dslContext = DSL.using(configuration);
        
        // 5. Use jOOQ with automatic mapping
        // Query and map to object
        User user = dslContext
            .resultQuery("SELECT * FROM users WHERE id = ?", 1)
            .fetchOne()
            .into(User.class);
        
        // Update object and map back to record
        user.setAge(31);
        Record record = dslContext.newRecord(USERS);
        record.from(user);
        
        // Insert or update
        dslContext.insertInto(USERS)
            .set(record)
            .execute();
    }
}
```

### Spring Boot Environment

With Spring Boot, everything is auto-configured:

#### 1. Add Dependency

Same as above (Maven or Gradle).

#### 2. Configure Application Properties

```yaml
skr:
  jooq:
    mapper:
      enabled: true                        # Enable auto-configuration (default: true)
      table-field-case-type: SNAKE_CASE    # Field naming convention (default: SNAKE_CASE)
```

#### 3. Auto-Configuration Happens Automatically

When `DSLContext` bean is present, `skr-jooq-mapper` automatically configures:
- `ConverterRegistry` with all built-in converters
- `SkrRecordMapperProvider` (Record → Model)
- `SkrRecordUnmapperProvider` (Model → Record)
- JSON converters using Spring's `ObjectMapper`

#### 4. Use Directly

```java
@Service
public class UserService {
    @Autowired
    private DSLContext dsl;
    
    public User findUser(Long id) {
        // Automatic mapping from Record to User
        return dsl.selectFrom(USERS)
            .where(USERS.ID.eq(id))
            .fetchOne()
            .into(User.class);
    }
    
    public void saveUser(User user) {
        // Automatic mapping from User to Record
        Record record = dsl.newRecord(USERS);
        record.from(user);
        
        dsl.insertInto(USERS)
            .set(record)
            .onDuplicateKeyUpdate()
            .set(record)
            .execute();
    }
}
```

#### 5. Register Custom Converters (Optional)

Simply define your converter as a Spring bean:

```java
@Configuration
public class MyConverterConfig {
    
    @Bean
    public SkrJooqConverter<MyType, String> myTypeConverter() {
        return new SkrJooqConverter<>() {
            @Override
            public int match(Type modelType, Type jooqType) {
                // Priority 20 to override built-in converters
                if (modelType == MyType.class && jooqType == String.class) {
                    return 20;
                }
                return MISMATCH;
            }
            
            @Override
            public String convertToJooqType(MyType mVal, Class<?> jooqType) {
                return mVal.serialize();
            }
            
            @Override
            public MyType convertToModelType(String jVal, Type modelType) {
                return MyType.deserialize(jVal);
            }
        };
    }
}
```

All `SkrJooqConverter` beans are automatically registered with the registry key `"custom"`.

## Configuration

### Table Field Case Type

Configure the naming convention for mapping database column names to Java field names:

```yaml
skr:
  jooq:
    mapper:
      table-field-case-type: SNAKE_CASE
```

**Available options:**
- `CAMEL_CASE` - `userName`
- `SNAKE_CASE` - `user_name` (default)
- `SCREAMING_SNAKE_CASE` - `USER_NAME`
- `KEBAB_CASE` - `user-name`

**Example:**

Database column `user_name` with `SNAKE_CASE` setting maps to Java field `userName`.

### Custom Converters

#### Implementing a Converter

Implement the `SkrJooqConverter` interface:

```java
public class CustomEnumConverter implements SkrJooqConverter<MyEnum, String> {
    
    @Override
    public int match(Type modelType, Type jooqType) {
        if (modelType == MyEnum.class && jooqType == String.class) {
            return 15; // Higher priority than built-in (10)
        }
        return MISMATCH;
    }
    
    @Override
    public String convertToJooqType(MyEnum mVal, Class<?> jooqType) {
        return mVal.name();
    }
    
    @Override
    public MyEnum convertToModelType(String jVal, Type modelType) {
        return MyEnum.valueOf(jVal);
    }
    
    @Override
    public boolean cacheable() {
        return true; // Allow caching of match results
    }
}
```

#### Registering Converters

**Pure Java:**
```java
converterRegistry.registerConverter(new CustomEnumConverter(), "my-module");
```

**Spring Boot:**
```java
@Bean
public SkrJooqConverter<MyEnum, String> customEnumConverter() {
    return new CustomEnumConverter();
}
```

#### Unregistering Converters

```java
converterRegistry.unregisterConverter("my-module");
```

### License

```
/*
 * Copyright (C) 2025-present the original author or authors.
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
```

