[![GitHub release](https://img.shields.io/github/release/hank-cp/skr-jooq-mapper.svg)](https://github.com/hank-cp/skr-jooq-mapper/releases)
![Maven Central](https://img.shields.io/maven-central/v/org.laxture.skr-jooq-mapper/skr-jooq-mapper.svg)
![Test](https://github.com/hank-cp/skr-jooq-mapper/workflows/CI%20Test/badge.svg)
![GitHub](https://img.shields.io/github/license/hank-cp/skr-jooq-mapper)
![GitHub last commit](https://img.shields.io/github/last-commit/hank-cp/skr-jooq-mapper.svg)

# skr-jooq-mapper

一个构建在 [jOOQ](https://www.jooq.org/) 之上的轻量级、可扩展的 ORM 框架,提供 jOOQ Records 与 Java POJO 之间的双向映射。

## 简介

`skr-jooq-mapper` 填补了 jOOQ 强大的 SQL 能力与面向对象领域模型之间的鸿沟。虽然 jOOQ 擅长类型安全的 SQL 操作,但它缺乏对复杂对象结构的灵活映射能力。该库通过提供以下功能填补了这一空白:

- **运行时可扩展的类型转换系统** - 动态注册和注销自定义转换器
- **一流的 JSON 支持** - 原生处理 JSON/JSONB 数据库类型,集成 Jackson
- **级联映射** - 使用字段名前缀自动映射嵌套对象
- **Spring Boot 自动配置** - 在 Spring Boot 应用中零配置设置

当您想要 jOOQ 的 SQL 能力与现代 ORM 的对象映射灵活性相结合时,请选择 `skr-jooq-mapper`。

## 核心特性

### 运行时动态自定义映射

与编译时转换器不同,`skr-jooq-mapper` 允许您在运行时注册和注销类型转换器:

- **基于优先级的匹配** - 多个转换器可以针对相同类型;最高优先级的转换器胜出
- **内置转换器覆盖** - 优先级 > 10 的自定义转换器可覆盖内置转换器
- **性能优化** - 转换器匹配结果被缓存以实现快速查找
- **生命周期管理** - 使用键注册转换器,便于批量移除

```java
// 注册自定义转换器
converterRegistry.registerConverter(new MyCustomConverter(), "my-app");

// 稍后,注销该模块的所有转换器
converterRegistry.unregisterConverter("my-app");
```

### 卓越的 JSON 支持

原生支持 jOOQ 的 JSON 和 JSONB 类型,无缝集成 Jackson:

- **自动 JSON 序列化/反序列化** - POJO、Map、List 和 JsonNode
- **类型安全映射** - JSON 数组映射到 `List<T>`,JSON 对象映射到 POJO 或 `Map<String, Object>`
- **持久化感知序列化** - 尊重 `@JsonTransient` 和其他瞬态注解
- **灵活的 JSON 处理** - 将 JSON 处理为对象、映射或原始字符串

```java
public class User {
    private List<Education> eduExperiences;  // 从 JSON 数组映射
    private Map<String, Object> metadata;     // 从 JSON 对象映射
    private JsonNode settings;                // 原始 JSON 节点
}
```

### 与其他 jOOQ 映射器库的对比

| 功能             | jOOQ 默认映射器 | ModelMapper | SimpleFlatMapper | skr-jooq-mapper |
|----------------|------------|-------------|------------------|-----------------|
| 自定义类型转换        | ❌ 有限       | ✅ 编译时       | ✅ 编译时            | ✅ 运行时动态         |
| JSON/JSONB 支持  | ❌ 基础       | ❌ 无         | ❌ 无              | ✅ 卓越 (Jackson)  |
| 嵌套对象映射         | ❌ 无        | ✅ 支持        | ✅ 支持             | ✅ 支持            |
| 运行时可扩展性        | ❌ 无        | ❌ 无         | ❌ 无              | ✅ 完全支持          |
| 性能             | ⚡ 快速       | ⚡ 快速        | ⚡ 快速             | ⚡ 快速 (缓存)       |
| Spring Boot 集成 | ❌ 手动       | ❌ 手动        | ❌ 手动             | ✅ 自动配置          |

### 为什么选择 jOOQ + Mapper 而非 JPA?

虽然 JPA(Java Persistence API)是一个流行的 ORM 标准,但它存在诸多限制,使其不太适合现代的、以数据库为中心的应用:

#### JPA 的局限性

- **笨重且难以控制** - JPA 实现(如 Hibernate)是复杂的框架,学习曲线陡峭,行为不透明
- **静态配置** - 无法在运行时更改实体映射、转换器或持久化策略
- **抽象层过厚** - ORM 抽象隐藏了 SQL,导致难以:
  - 理解实际执行的查询
  - 优化复杂查询或连接
  - 调试性能问题
  - 控制数据库特定行为
- **数据库特性利用受限** - JPA 的数据库无关性方法阻止您利用现代数据库能力:
  - **PostgreSQL**: JSONB 操作符、数组函数、CTE、窗口函数、全文搜索
  - **MySQL/MariaDB**: JSON 函数、空间数据类型
  - **Oracle**: 高级分析、层次化查询
- **N+1 查询问题** - 容易意外触发损害性能的查询模式
- **延迟加载问题** - 会话管理复杂,特别是在 Web 应用中

#### jOOQ + skr-jooq-mapper 的优势

| 维度 | JPA | jOOQ + skr-jooq-mapper |
|------|-----|------------------------|
| **控制力** | ❌ 不透明的 SQL 生成 | ✅ 完全的 SQL 控制与类型安全 DSL |
| **运行时灵活性** | ❌ 静态实体映射 | ✅ 动态转换器注册 |
| **数据库特性** | ❌ 最低公分母 | ✅ 数据库特定优化 |
| **性能调优** | ❌ 查询提示、复杂连接受限 | ✅ 直接 SQL 优化 |
| **学习曲线** | ❌ 高(HQL、JPQL、生命周期) | ✅ 较低(只需 SQL + Java) |
| **类型安全** | ✅ 编译时(实体) | ✅ 编译时(SQL + 映射) |
| **现代数据库类型** | ❌ 有限(需要自定义类型) | ✅ 一流的 JSON、数组、自定义类型 |

#### 何时选择 jOOQ + Mapper

当您需要以下功能时,选择 jOOQ 与 skr-jooq-mapper:

- 🎯 **完全 SQL 控制** - 复杂查询、分析、报表
- ⚡ **最大性能** - 每个查询都很重要
- 🔧 **数据库特定特性** - JSONB、数组、窗口函数、CTE
- 🚀 **运行时灵活性** - 多租户应用、动态模式
- 📊 **以数据为中心的应用** - 读多写少、复杂聚合

### 为什么选择 jOOQ + Mapper 而非 MyBatis?

MyBatis 是一个流行的 SQL 映射框架,但它缺乏现代化的类型安全和生产力特性:

| 功能 | MyBatis | jOOQ + skr-jooq-mapper |
|------|---------|------------------------|
| **类型安全** | ❌ 无编译时 SQL 检查 | ✅ 类型安全的 SQL DSL |
| **SQL 编写** | ❌ XML/注解,容易出错 | ✅ Java DSL,IDE 友好 |
| **编译时检查** | ❌ SQL 错误在运行时发现 | ✅ Schema 变更时编译失败 |
| **结果映射** | ❌ 手动 XML/注解映射 | ✅ 自动双向映射 |
| **类型转换** | ❌ 有限,需要自定义处理器 | ✅ 可扩展的运行时转换器 |
| **重构支持** | ❌ XML 字符串在重命名时损坏 | ✅ IDE 重构支持 |
| **JSON 支持** | ❌ 手动序列化 | ✅ 原生 JSON/JSONB 映射 |
| **运行时扩展性** | ❌ 静态配置 | ✅ 动态转换器注册 |
| **学习曲线** | ✅ 简单(只需 SQL) | ⚖️ 中等(SQL + DSL) |

#### MyBatis 的痛点

- **无编译时安全** - SQL 拼写错误、列名错误、类型不匹配只能在运行时发现
- **XML 维护负担** - SQL 分散在 XML 文件中,难以导航和重构
- **手动结果映射** - 为复杂对象配置繁琐的 `<resultMap>`
- **类型转换薄弱** - 每个非标准类型都需要自定义 `TypeHandler`
- **IDE 支持差** - XML 中的 SQL 无自动完成、导航或重构
- **Schema 演进摩擦** - 数据库变更在运行时而非编译时出错

#### jOOQ + skr-jooq-mapper 的优势

```java
// MyBatis - 可能出现运行时错误
@Select("SELECT user_name FROM users WHERE id = #{id}") // 拼写错误:应该是 username
User findUser(Long id);

// jOOQ - 编译时安全
User user = dsl.select(USERS.USERNAME) // 如果列不存在则编译错误
    .from(USERS)
    .where(USERS.ID.eq(id))
    .fetchOne()
    .into(User.class); // 自动类型安全映射
```

**核心优势:**

- 🛡️ **类型安全** - 数据库 schema 变更导致编译错误,而非生产环境 bug
- 🔄 **自动映射** - 大多数情况下无需手动配置 `<resultMap>` 或 `TypeHandler`
- 📝 **更好的工具支持** - 完整的 IDE 支持:自动完成、重构、导航
- 🎨 **更清晰的代码** - SQL 与 Java 一起,无需 XML 上下文切换
- ⚙️ **运行时灵活性** - 动态注册自定义转换器
- 🚀 **现代类型** - 一流的 JSON、时间类型、自定义转换器

#### 何时选择 jOOQ + Mapper

在以下情况下选择 jOOQ 与 skr-jooq-mapper 而非 MyBatis:

- ✅ 您想要**编译时安全**和早期错误检测
- ✅ 您的数据库 schema **经常变更**
- ✅ 您需要**复杂的类型转换**(JSON、自定义类型)
- ✅ 您重视 **IDE 支持**和重构能力
- ✅ 您更喜欢 **Java 中的 SQL** 而非 XML/注解

坚持使用 MyBatis 如果:

- ⚠️ 您有遗留的 XML 映射且没有迁移预算
- ⚠️ 您的团队强烈倾向于将 SQL 放在单独的文件中
- ⚠️ 您只使用简单的 POJO 映射和基本类型

### 其他特性

#### 级联映射
使用字段名分隔符(默认: `_`)映射嵌套对象:

```java
public class User {
    private String name;
    private Address address;  // 从 address_* 字段映射
}

public class Address {
    private String line1;     // 从 address_line1 映射
    private String city;      // 从 address_city 映射
}
```

#### 字段命名约定支持
自动转换数据库和 Java 命名风格:
- `SNAKE_CASE` - `user_name` (默认)
- `CAMEL_CASE` - `userName`
- `SCREAMING_SNAKE_CASE` - `USER_NAME`
- `KEBAB_CASE` - `user-name`

#### 丰富的注解支持
对映射行为的细粒度控制:

- **`@Transient`** - 从持久化和加载中排除字段
- **`@JsonTransient`** - 仅从 JSON 序列化中排除
- **`@LeftoverCollector`** - 将未映射的列收集到 `Map<String, Object>` 中
- **`@MappingInstantiator`** - 指定用于对象创建的构造函数/构建器
- **`@PrimaryKey`** - 标记主键字段(null PK = null 嵌套对象)
- **`@Immutable`** - 将字段标记为只读(更新时跳过)

## 快速开始

### 安装

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

### 纯 Java 环境

在非 Spring 应用中设置 `skr-jooq-mapper` 的完整示例:

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
        // 1. 定义您的模型
        class User {
            private Long id;
            private String name;
            private int age;
            
            // Getters 和 setters...
        }
        
        // 2. 创建 ConverterRegistry 并注册转换器
        ObjectMapper objectMapper = ObjectMapperConfigurer
            .setupPersistentObjectMapper(new ObjectMapper());
        
        ConverterRegistry converterRegistry = new ConverterRegistry();
        converterRegistry.registerConverter(new JsonObjectConverter(objectMapper), null);
        converterRegistry.registerConverter(new JsonArrayConverter(objectMapper), null);
        converterRegistry.registerConverter(new JsonObject2MapConverter(objectMapper), null);
        converterRegistry.registerConverter(new JsonbObjectConverter(objectMapper), null);
        converterRegistry.registerConverter(new JsonbArrayConverter(objectMapper), null);
        converterRegistry.registerConverter(new JsonbObject2MapConverter(objectMapper), null);
        
        // 3. 创建映射器和反映射器提供者
        SkrRecordMapperProvider mapperProvider = new SkrRecordMapperProvider(
            converterRegistry,
            TableFieldCaseType.SNAKE_CASE,
            objectMapper
        );
        
        DSLContextProvider dslContextProvider = () -> dslContext; // 您的 DSLContext 实例
        SkrRecordUnmapperProvider unmapperProvider = new SkrRecordUnmapperProvider(
            dslContextProvider,
            converterRegistry,
            TableFieldCaseType.SNAKE_CASE,
            objectMapper
        );
        
        // 4. 使用提供者配置 jOOQ
        Connection connection = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");
        Configuration configuration = new org.jooq.impl.DefaultConfiguration()
            .set(SQLDialect.H2)
            .set(connection)
            .set(mapperProvider)
            .set(unmapperProvider);
        
        DSLContext dslContext = DSL.using(configuration);
        
        // 5. 使用带有自动映射的 jOOQ
        // 查询并映射到对象
        User user = dslContext
            .resultQuery("SELECT * FROM users WHERE id = ?", 1)
            .fetchOne()
            .into(User.class);
        
        // 更新对象并映射回记录
        user.setAge(31);
        Record record = dslContext.newRecord(USERS);
        record.from(user);
        
        // 插入或更新
        dslContext.insertInto(USERS)
            .set(record)
            .execute();
    }
}
```

### Spring Boot 环境

使用 Spring Boot,一切都是自动配置的:

#### 1. 添加依赖

与上述相同 (Maven 或 Gradle)。

#### 2. 配置应用属性

```yaml
skr:
  jooq:
    mapper:
      enabled: true                        # 启用自动配置 (默认: true)
      table-field-case-type: SNAKE_CASE    # 字段命名约定 (默认: SNAKE_CASE)
```

#### 3. 自动配置自动进行

当 `DSLContext` bean 存在时,`skr-jooq-mapper` 自动配置:
- 带有所有内置转换器的 `ConverterRegistry`
- `SkrRecordMapperProvider` (Record → Model)
- `SkrRecordUnmapperProvider` (Model → Record)
- 使用 Spring 的 `ObjectMapper` 的 JSON 转换器

#### 4. 直接使用

```java
@Service
public class UserService {
    @Autowired
    private DSLContext dsl;
    
    public User findUser(Long id) {
        // 从 Record 自动映射到 User
        return dsl.selectFrom(USERS)
            .where(USERS.ID.eq(id))
            .fetchOne()
            .into(User.class);
    }
    
    public void saveUser(User user) {
        // 从 User 自动映射到 Record
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

#### 5. 注册自定义转换器 (可选)

只需将您的转换器定义为 Spring bean:

```java
@Configuration
public class MyConverterConfig {
    
    @Bean
    public SkrJooqConverter<MyType, String> myTypeConverter() {
        return new SkrJooqConverter<>() {
            @Override
            public int match(Type modelType, Type jooqType) {
                // 优先级 20 以覆盖内置转换器
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

所有 `SkrJooqConverter` bean 都会自动注册,注册表键为 `"custom"`。

## 配置

### 表字段命名类型

配置数据库列名到 Java 字段名的命名约定:

```yaml
skr:
  jooq:
    mapper:
      table-field-case-type: SNAKE_CASE
```

**可用选项:**
- `CAMEL_CASE` - `userName`
- `SNAKE_CASE` - `user_name` (默认)
- `SCREAMING_SNAKE_CASE` - `USER_NAME`
- `KEBAB_CASE` - `user-name`

**示例:**

使用 `SNAKE_CASE` 设置,数据库列 `user_name` 映射到 Java 字段 `userName`。

### 自定义转换器

#### 实现转换器

实现 `SkrJooqConverter` 接口:

```java
public class CustomEnumConverter implements SkrJooqConverter<MyEnum, String> {
    
    @Override
    public int match(Type modelType, Type jooqType) {
        if (modelType == MyEnum.class && jooqType == String.class) {
            return 15; // 比内置转换器 (10) 更高的优先级
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
        return true; // 允许缓存匹配结果
    }
}
```

#### 注册转换器

**纯 Java:**
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

#### 注销转换器

```java
converterRegistry.unregisterConverter("my-module");
```

### 许可证

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
