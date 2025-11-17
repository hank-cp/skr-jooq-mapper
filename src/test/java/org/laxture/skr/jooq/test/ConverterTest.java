package org.laxture.skr.jooq.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.JSON;
import org.junit.jupiter.api.Test;
import org.laxture.skr.jooq.mapper.converter.json.JsonArrayConverter;
import org.laxture.skr.jooq.mapper.converter.json.JsonObjectConverter;
import org.laxture.skr.jooq.mapper.converter.json.JsonbArrayConverter;
import org.laxture.skr.jooq.mapper.converter.json.JsonbObjectConverter;
import org.laxture.skr.jooq.mapper.misc.RefectionUtils;
import org.laxture.skr.jooq.test.model.User;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ConverterTest {

    public User user;
    public List<User> users;

    @Test
    public void testJsonObjectConverter() {
        var converter = new JsonObjectConverter<>(new ObjectMapper(), User.class);
        assertThat(converter.getModelType(), is(User.class));
        assertThat(converter.match(RefectionUtils.findField(ConverterTest.class, "user").getGenericType(), org.jooq.JSON.class), is(10));
        User user = converter.convertToModelType(JSON.json("{\"id\":1}"));
        assertThat(user, notNullValue());
        assertThat(user.getId(), is(1L));

        var converter_ = new JsonbObjectConverter<>(new ObjectMapper(), User.class);
        assertThat(converter_.getModelType(), is(User.class));
        assertThat(converter_.match(RefectionUtils.findField(ConverterTest.class, "user").getGenericType(), org.jooq.JSONB.class), is(10));
    }

    @Test
    public void testJsonArrayConverter() {
        var converter = new JsonArrayConverter<>(new ObjectMapper(), User.class);
        assertThat(converter.match(RefectionUtils.findField(ConverterTest.class, "users").getGenericType(), org.jooq.JSON.class), is(11));
        List<User> users = converter.convertToModelType(JSON.json("[{\"id\":1}]"));
        assertThat(users, hasSize(1));
        assertThat(users.get(0).getId(), is(1L));

        var converter_ = new JsonbArrayConverter<>(new ObjectMapper(), User.class);
        assertThat(converter_.match(RefectionUtils.findField(ConverterTest.class, "users").getGenericType(), org.jooq.JSONB.class), is(11));
    }
}
