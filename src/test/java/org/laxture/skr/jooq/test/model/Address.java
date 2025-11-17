package org.laxture.skr.jooq.test.model;

import lombok.Data;
import org.laxture.skr.jooq.mapper.annotation.MappingInstantiator;

@Data
public class Address {
    private long id;
    private String line1;
    private String line2;
    private String city;

    private UserProfile userProfile;

    @MappingInstantiator
    public static Address of() {
        return new Address();
    }
}
