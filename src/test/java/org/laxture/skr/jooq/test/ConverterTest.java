package org.laxture.skr.jooq.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.apache.commons.beanutils.ConvertUtils;
import org.jooq.JSON;
import org.junit.jupiter.api.Test;
import org.laxture.skr.jooq.mapper.converter.ArrayConverter;
import org.laxture.skr.jooq.mapper.converter.ConverterRegistry;
import org.laxture.skr.jooq.mapper.converter.SkrJooqConverter;
import org.laxture.skr.jooq.mapper.converter.json.JsonArrayConverter;
import org.laxture.skr.jooq.mapper.converter.json.JsonObjectConverter;
import org.laxture.skr.jooq.mapper.converter.json.JsonbArrayConverter;
import org.laxture.skr.jooq.mapper.converter.json.JsonbObjectConverter;
import org.laxture.skr.jooq.mapper.misc.ObjectMapperConfigurer;
import org.laxture.skr.jooq.mapper.misc.RefectionUtils;
import org.laxture.skr.jooq.test.model.User;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ConverterTest {

    public User user;
    public List<User> users;
    public long[] userIds;
    public String[] userNames;
    public User[] userArray;
    public List<Long> friendIds;
    public List<String> friendNames;

    @Test
    public void testJsonObjectConverter() {
        var converter = new JsonObjectConverter(new ObjectMapper());
        assertThat(converter.match(RefectionUtils.findField(ConverterTest.class, "user").getGenericType(), org.jooq.JSON.class), is(11));
        User user = (User) converter.convertToModelType(JSON.json("{\"id\":1}"), User.class);
        assertThat(user, notNullValue());
        assertThat(user.getId(), is(1L));

        var converter_ = new JsonbObjectConverter(new ObjectMapper());
        assertThat(converter_.match(RefectionUtils.findField(ConverterTest.class, "user").getGenericType(), org.jooq.JSONB.class), is(11));
    }

    @Test
    public void testJsonArrayConverter() {
        Type userListType = RefectionUtils.findField(ConverterTest.class, "users").getGenericType();

        var converter = new JsonArrayConverter(new ObjectMapper());
        assertThat(converter.match(userListType, org.jooq.JSON.class), is(12));
        List<User> users = (List<User>) converter.convertToModelType(JSON.json("[{\"id\":1}]"), userListType);
        assertThat(users, hasSize(1));
        assertThat(users.get(0).getId(), is(1L));

        var converter_ = new JsonbArrayConverter(new ObjectMapper());
        assertThat(converter_.match(userListType, org.jooq.JSONB.class), is(12));
    }

    @Test
    public void testArrayConverter() {
        Type userIdsType = RefectionUtils.findField(ConverterTest.class, "userIds").getGenericType();
        Type friendIdsType = RefectionUtils.findField(ConverterTest.class, "friendIds").getGenericType();
        Type friendNamesType = RefectionUtils.findField(ConverterTest.class, "friendNames").getGenericType();

        var converter = new ArrayConverter();

        // test match
        assertThat(converter.match(userIdsType, Object[].class), is(11));
        assertThat(converter.match(userIdsType, int[].class), is(11));
        assertThat(converter.match(userIdsType, long[].class), is(11));

        // test convertToJooqType
        int[] c_1 = (int[]) converter.convertToJooqType(new int[]{1, 2, 3}, int[].class);
        assertThat(c_1, notNullValue());
        assertThat(c_1.length, is(3));
        assertThat(c_1[0], is(1));

        int[] c_2 = (int[]) converter.convertToJooqType(List.of(1, 2, 3), int[].class);
        assertThat(c_2, notNullValue());
        assertThat(c_2.length, is(3));
        assertThat(c_2[0], is(1));

        // int[] to long[]
        long[] c_3 = (long[]) converter.convertToModelType(new int[]{1, 2, 3}, userIdsType);
        assertThat(c_3, notNullValue());
        assertThat(c_3.length, is(3));
        assertThat(c_3[0], is(1L));

        // int[] to String[]
        String[] c_4 = (String[]) converter.convertToJooqType(new int[]{1, 2, 3}, String[].class);
        assertThat(c_4, notNullValue());
        assertThat(c_4.length, is(3));
        assertThat(c_4[0], is("1"));

        // int[] to List<Long>
        List<Long> c_5 = (List<Long>) converter.convertToModelType(new int[]{1, 2, 3}, friendIdsType);
        assertThat(c_5, notNullValue());
        assertThat(c_5.size(), is(3));
        assertThat(c_5.get(0), is(1L));

        // int[] to List<String>
        List<String> c_6 = (List<String>) converter.convertToModelType(new int[]{1, 2, 3}, friendNamesType);
        assertThat(c_6, notNullValue());
        assertThat(c_6.size(), is(3));
        assertThat(c_6.get(0), is("1"));

        // String[] to List<String>
        List<String> c_7 = (List<String>) converter.convertToModelType(new String[]{"1", "2", "3"}, friendNamesType);
        assertThat(c_7, notNullValue());
        assertThat(c_7.size(), is(3));
        assertThat(c_7.get(0), is("1"));

        // String[] to List<Long>
        List<Long> c_8 = (List<Long>) converter.convertToModelType(new String[]{"1", "2", "3"}, friendIdsType);
        assertThat(c_8, notNullValue());
        assertThat(c_8.size(), is(3));
        assertThat(c_8.get(0), is(1L));
    }

    @Test
    public void testConvertUtils() {
        // int[] to long[]
        long[] c_1 = (long[]) ConvertUtils.convert(new int[]{1, 2, 3}, long[].class);
        assertThat(c_1, notNullValue());
        assertThat(c_1.length, is(3));
        assertThat(c_1[0], is(1L));

        // array to String is not worked
//        String c_2 = (String) ConvertUtils.convert(new int[]{1, 2, 3}, String.class);
//        assertThat(c_2, notNullValue());
//        assertThat(c_2, is("1,2,3"));

        // string to long[]
        long[] c_3 = (long[]) ConvertUtils.convert("1,2,3", long[].class);
        assertThat(c_3, notNullValue());
        assertThat(c_3.length, is(3));
        assertThat(c_3[0], is(1L));

        // array to List is not worked
//        List<Long> c_4 = (List<Long>) ConvertUtils.convert(new int[]{1, 2, 3}, List.class);
//        assertThat(c_4, notNullValue());
//        assertThat(c_4.size(), is(3));
//        assertThat(c_4.get(0), is(1L));

        // int[] to String[]
        String[] c_5 = (String[]) ConvertUtils.convert(new int[]{1, 2, 3}, String[].class);
        assertThat(c_5, notNullValue());
        assertThat(c_5.length, is(3));
        assertThat(c_5[0], is("1"));

        // String[] to int[]
        int[] c_6 = (int[]) ConvertUtils.convert(new String[]{"1", "2", "3"}, int[].class);
        assertThat(c_6, notNullValue());
        assertThat(c_6.length, is(3));
        assertThat(c_6[0], is(1));

        // binInteger to long
        assertThat(ConvertUtils.convert(BigInteger.valueOf(1L), long.class), is(1L));
        // bigDecimal to double
        assertThat(ConvertUtils.convert(BigDecimal.valueOf(1d), double.class), is(1d));
    }

    @Test
    public void testObjectMapper() throws Exception {
        ObjectMapper objectMapper = ObjectMapperConfigurer.setupPersistentObjectMapper(new ObjectMapper());

        Map map = objectMapper.readValue("{\"a\":1}", Map.class);
        assertThat(map, notNullValue());
        assertThat(map.get("a"), is(1));

        List list = objectMapper.readValue("[1,2,3]", List.class);
        assertThat(list, notNullValue());
        assertThat(list.size(), is(3));
        assertThat(list.get(0), is(1));

        List<Map> listMap = objectMapper.readValue("[{\"a\":1},{\"a\":2},{\"a\":3}]", List.class);
        assertThat(listMap, notNullValue());
        assertThat(listMap.size(), is(3));
        assertThat(listMap.get(0).get("a"), is(1));
    }

    /**
     * 测试 ConverterRegistry 缓存命中
     * 第一次调用 matchConverter() 后，第二次用相同参数调用应该返回相同的 converter 实例（缓存命中）
     */
    @Test
    public void testConverterRegistryCacheHit() {
        ConverterRegistry registry = new ConverterRegistry();

        // 第一次调用 matchConverter()
        SkrJooqConverter<?, ?> converter1 = registry.matchConverter(String.class, String.class);
        assertThat(converter1, notNullValue());

        Map cache = RefectionUtils.getFieldValue(registry, "converterCache");
        assertThat(cache.size(), is(1));
    }

    /**
     * 测试注册新 converter 后缓存失效
     * 注册新的 converter 后，缓存应该被清除，matchConverter() 可能返回新注册的 converter
     */
    @Test
    public void testConverterRegistryCacheInvalidationOnRegister() {
        ConverterRegistry registry = new ConverterRegistry();

        // 第一次调用建立缓存
        SkrJooqConverter<?, ?> originalConverter = registry.matchConverter(String.class, String.class);
        assertThat(originalConverter, notNullValue());
        Map cache = RefectionUtils.getFieldValue(registry, "converterCache");
        assertThat(cache.size(), is(1));

        // 注册一个高优先级的自定义 converter
        SkrJooqConverter<String, String> customConverter = new SkrJooqConverter<>() {
            @Override
            public int match(Type modelType, Type jooqType) {
                if (modelType == String.class && jooqType == String.class) {
                    return 100; // 高优先级
                }
                return MISMATCH;
            }

            @Override
            public String convertToJooqType(@NonNull String mVal, Class<?> jooqType) {
                return mVal;
            }

            @Override
            public String convertToModelType(@NonNull String jVal, Type modelType) {
                return jVal;
            }
        };

        registry.registerConverter(customConverter, "test-key");

        // 注册新 converter 后，缓存应该被清除，返回新的高优先级 converter
        SkrJooqConverter<?, ?> newConverter = registry.matchConverter(String.class, String.class);
        assertThat(newConverter, notNullValue());
        assertThat(newConverter, sameInstance(customConverter));
        assertThat(newConverter, not(sameInstance(originalConverter)));
        cache = RefectionUtils.getFieldValue(registry, "converterCache");
        assertThat(cache.size(), is(1));

        // 注销自定义 converter
        registry.unregisterConverter("test-key");
        cache = RefectionUtils.getFieldValue(registry, "converterCache");
        assertThat(cache.size(), is(0));
        // 缓存应该被清除，返回原始的内置 converter
        SkrJooqConverter<?, ?> converter2 = registry.matchConverter(String.class, String.class);
        assertThat(converter2, notNullValue());
        assertThat(converter2, not(sameInstance(customConverter)));
    }

    /**
     * 测试 clearCache() 方法
     * 显式调用 clearCache() 后，缓存应该被清除
     */
    @Test
    public void testConverterRegistryClearCache() {
        ConverterRegistry registry = new ConverterRegistry();

        // 第一次调用建立缓存
        SkrJooqConverter<?, ?> converter1 = registry.matchConverter(String.class, String.class);
        assertThat(converter1, notNullValue());
        Map cache = RefectionUtils.getFieldValue(registry, "converterCache");
        assertThat(cache.size(), is(1));

        // 验证缓存命中
        SkrJooqConverter<?, ?> converter2 = registry.matchConverter(String.class, String.class);
        assertThat(converter2, sameInstance(converter1));

        // 显式清除缓存
        registry.clearCache();
        cache = RefectionUtils.getFieldValue(registry, "converterCache");
        assertThat(cache.size(), is(0));
    }
}
