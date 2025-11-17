package org.laxture.skr.jooq.test.model;

import lombok.Data;
import org.laxture.skr.jooq.mapper.annotation.JsonTransient;

import java.time.LocalDate;

@Data
public class EducationExperience {

    public LocalDate startDate;
    public LocalDate endDate;
    public String institute;
    public String major;

    @JsonTransient
    public String jsonTransient;
}
