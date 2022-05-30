package icu.lowcoder.spring.cloud.organization.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AuthorityResponse {
    private UUID id;
    private String name;
    private String code;
    private String description;

    private Boolean builtIn;
}
