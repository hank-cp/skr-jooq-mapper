package org.laxture.skr.jooq.test.model;

import lombok.Data;
import org.laxture.skr.jooq.mapper.annotation.JsonTransient;

@Data
public class EducationExperience {

    public String institute;
    public String major;

    @JsonTransient
    public String jsonTransient;
}
