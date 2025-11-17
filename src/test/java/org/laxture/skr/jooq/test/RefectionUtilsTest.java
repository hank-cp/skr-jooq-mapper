package org.laxture.skr.jooq.test;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
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

class RefectionUtilsTest {

    @Test
    public void callMethod() {
        String testStr = "asdf";
        String newStr = RefectionUtils.callMethod(testStr, "replaceAll", "a", "b");
        assertThat(newStr, equalTo("bsdf"));
    }

    @Test
    public void testIsPrimitive() {
        Double d1 = 1.0;
        double d2 = 1.0;
        assertThat(RefectionUtils.isPrimitive(d1), equalTo(true));
        assertThat(RefectionUtils.isPrimitive(d2), equalTo(true));
        assertThat(RefectionUtils.isString("string"), equalTo(true));
    }

    public static class A {
        static int s = -1;
        Map<String, String> map = new HashMap<>();
        B b = new B();
        C c = new C();
    }

    public static class B {
        Map<String, C> map = new HashMap<>();
        List<String> list = new ArrayList<>();
        List<C> cList = new ArrayList<>();
    }

    public static class C {
        String d;

        public String getEval() {
            return d;
        }

        public boolean isFval() {
            return true;
        }
    }

    public static class D extends A {

    }

    @Test
    public void testGetFieldValue() {
        A a = new A();
        a.map.put("key", "value");
        a.b.list.add("li1");
        a.c.d = "c0";
        C c1 = new C();
        c1.d = "c1";
        a.b.cList.add(c1);
        C c2 = new C();
        c2.d = "c2";
        a.b.map.put("c2", c2);
        C c3 = new C();
        c3.d = "c3";
        a.b.map.put("c3", c3);

        assertThat(RefectionUtils.getFieldValue(a, "c.d"), equalTo("c0"));
        assertThat(RefectionUtils.getFieldValue(a, "map.*"), hasItem("value"));
        assertThat(RefectionUtils.getFieldValue(a, "map.key"), equalTo("value"));
        assertThat(RefectionUtils.getFieldValue(a, "b.list"), hasItem("li1"));
        assertThat(RefectionUtils.getFieldValue(a, "b.cList.d"), hasItem("c1"));
        assertThat(RefectionUtils.getFieldValue(a, "b.cList.*.d"), hasItem("c1"));
        assertThat(RefectionUtils.getFieldValue(a, "b.map.*.d"), allOf(
            (Matcher) hasSize(2), hasItem("c2"), hasItem("c3")));
        assertThat(RefectionUtils.getFieldValue(a, "c.eval"), equalTo("c0"));
        assertThat(RefectionUtils.getFieldValue(a, "c.fval"), equalTo(true));
    }

    @Test
    public void testGetStaticValue() {
        assertThat(RefectionUtils.getFieldValue(A.class, "s"), equalTo(-1));
        assertThat(RefectionUtils.getFieldValue(D.class, "s"), equalTo(-1));
    }

    @Test
    void testFindMatchModelField() {
        // find first level field
        User user = new User();
        RefectionUtils.FieldTuple modelField =
            RefectionUtils.findMatchModelField(user, "name");
        assertThat(modelField, notNullValue());
        assertThat(modelField.getField(), notNullValue());
        assertThat(modelField.getField().getName(), equalTo("name"));
        assertThat(modelField.getOwner(), is(user));

        // find nested field
        user = new User();
        modelField = RefectionUtils.findMatchModelField(user, "addressCity");
        assertThat(modelField, notNullValue());
        assertThat(modelField.getField(), notNullValue());
        assertThat(modelField.getField().getName(), equalTo("city"));
        assertThat(modelField.getOwner(), notNullValue());
        assertThat(modelField.getOwner(), instanceOf(Address.class));
        assertThat(user.getAddress(), nullValue());
        modelField.settle(); // settle nested object
        assertThat(user.getAddress(), notNullValue());
        assertThat(user.getAddress(), is(modelField.getOwner()));


        // find nested field with two words
        user = new User();
        modelField = RefectionUtils.findMatchModelField(user, "userProfileAvatarUrl");
        assertThat(modelField, notNullValue());
        assertThat(modelField.getField(), notNullValue());
        assertThat(modelField.getField().getName(), equalTo("avatarUrl"));
        assertThat(modelField.getOwner(), notNullValue());
        assertThat(modelField.getOwner(), instanceOf(UserProfile.class));
        assertThat(user.getUserProfile(), nullValue());
        modelField.settle(); // settle nested object
        assertThat(user.getUserProfile(), notNullValue());
        assertThat(user.getUserProfile(), is(modelField.getOwner()));

        // find nested field with multiple levels
        user = new User();
        modelField = RefectionUtils.findMatchModelField(user, "addressUserProfileAvatarUrl");
        assertThat(modelField, notNullValue());
        assertThat(modelField.getField(), notNullValue());
        assertThat(modelField.getField().getName(), equalTo("avatarUrl"));
        assertThat(modelField.getOwner(), notNullValue());
        assertThat(modelField.getOwner(), instanceOf(UserProfile.class));
        assertThat(user.getAddress(), nullValue());
        modelField.settle(); // settle nested object
        assertThat(user.getAddress(), notNullValue());
        assertThat(user.getAddress().getUserProfile(), notNullValue());
        assertThat(user.getAddress().getUserProfile(), is(modelField.getOwner()));

        // find non-exist field
        user = new User();
        modelField = RefectionUtils.findMatchModelField(user, "nonExistField");
        assertThat(modelField, nullValue());

        // find non-exist nested field
        user = new User();
        modelField = RefectionUtils.findMatchModelField(user, "addressNonExistField");
        assertThat(modelField, nullValue());
        assertThat(user.getAddress(), nullValue());
    }
}
