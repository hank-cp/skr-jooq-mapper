package org.laxture.skr.jooq.test.model;

import lombok.Data;
import org.laxture.skr.jooq.mapper.annotation.LeftoverCollector;
import org.laxture.skr.jooq.mapper.annotation.Transient;
import org.laxture.skr.jooq.mapper.hook.MappingHook;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class User implements MappingHook {

    private long id;

    private String name;
    private Integer age;

    // test java.time
    private LocalDateTime createdAt;
    private LocalDate birthDate;

    // test Nested Object
    private Address address;

    // test Nested Object with two words in field name
    private UserProfile userProfile;

    // Test Json to object list
    private List<EducationExperience> eduExperiences;

    // Test Json object
    private EducationExperience recentEducExperience;

    // Test Json to Map
    private Map<String, Object> metaInfo;

    // Test Json to primitive list
    private List<Long> friendIds;

    @LeftoverCollector
    private Map<String, Object> extras;

    @Transient
    private boolean postMappingCalled = false;

    public User() {
    }

    @Override
    public void postMapping() {
        this.postMappingCalled = true;
    }
}
