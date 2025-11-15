package org.laxture.skr.jooq.test.model;

import org.laxture.skr.jooq.mapper.annotation.LeftoverCollector;
import org.laxture.skr.jooq.mapper.hook.MappingHook;

import java.util.Map;

public class User implements MappingHook {
    private String name;
    private Integer age;
    private Address address;

    @LeftoverCollector
    private Map<String, Object> extras;

    private boolean postMappingCalled = false;

    public User() {
    }

    @Override
    public void postMapping() {
        this.postMappingCalled = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Map<String, Object> getExtras() {
        return extras;
    }

    public void setExtras(Map<String, Object> extras) {
        this.extras = extras;
    }

    public boolean isPostMappingCalled() {
        return postMappingCalled;
    }
}
