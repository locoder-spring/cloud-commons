package icu.lowcoder.spring.cloud.organization.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class EditEmployeeRequest {
    @NotBlank
    private String name;

    private String email;
    private String no;

    private List<UUID> departments;
    private List<UUID> roles;
    private List<UUID> authorities;
}
