package icu.lowcoder.spring.cloud.organization.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class ManagementUserModel {
    private UUID id;
    private UUID employeeId;
    private String no;
    private String name;
    private String email;
    private Boolean enabled;

    private Set<String> authorities = new HashSet<>();
    private Set<String> roles = new HashSet<>();
}
