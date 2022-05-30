package icu.lowcoder.spring.cloud.organization.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
public class AuthorityModel implements Serializable {
    private UUID id;
    private String name;
    private String code;
    private String description;
}
