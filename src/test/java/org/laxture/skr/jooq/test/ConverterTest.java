package org.laxture.skr.jooq.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.beanutils.ConvertUtils;
import org.jooq.JSON;
import org.junit.jupiter.api.Test;
import org.laxture.skr.jooq.mapper.converter.ArrayConverter;
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
}
