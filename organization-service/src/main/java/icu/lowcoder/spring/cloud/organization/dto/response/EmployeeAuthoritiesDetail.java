package icu.lowcoder.spring.cloud.organization.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Getter
@Setter
public class EmployeeAuthoritiesDetail {
    private UUID id;
    private String name;
    private String phone;
    private String email;
    private String no;
    private Boolean enabled;

    private Collection<String> authorities = new ArrayList<>();
}
