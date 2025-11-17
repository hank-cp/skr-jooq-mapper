package org.laxture.skr.jooq.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.laxture.skr.jooq.mapper.converter.json.JsonObjectConverter;
import org.laxture.skr.jooq.mapper.misc.RefectionUtils;
import org.laxture.skr.jooq.test.model.Address;
import org.laxture.skr.jooq.test.model.User;
import org.laxture.skr.jooq.test.model.UserProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ConverterTest {

    @Test
    public void testJsonObjectConverter() {
        var converter = new JsonObjectConverter<User>(new ObjectMapper());
        assertThat(converter.getModelType(), is(User.class));
    }
}
