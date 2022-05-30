package icu.lowcoder.spring.cloud.organization.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RoleResponse {
    private UUID id;
    private String name;
    private String code;
    private String description;

    private Boolean builtIn;

    private List<UUID> authorities;
}
