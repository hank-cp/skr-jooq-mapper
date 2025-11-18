package org.laxture.skr.jooq.test.model;

import org.laxture.skr.jooq.mapper.annotation.JsonTransient;

public class EducationExperience {

    public String institute;
    public String major;

    @JsonTransient
    public String jsonTransient;

    public static EducationExperience of(String institute, String major,
                                         String jsonTransient) {
        EducationExperience eduExp = new EducationExperience();
        eduExp.institute = institute;
        eduExp.major = major;
        eduExp.jsonTransient = jsonTransient;
        return eduExp;
    }
}
