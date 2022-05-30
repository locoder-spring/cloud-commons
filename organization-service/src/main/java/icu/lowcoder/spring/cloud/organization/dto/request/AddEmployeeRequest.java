package icu.lowcoder.spring.cloud.organization.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.RegEx;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AddEmployeeRequest {
    @NotBlank
    @Pattern(regexp = "\\d{11}")
    private String phone;
    @NotBlank
    private String name;

    private String email;
    private String no;

    private List<UUID> departments;
    private List<UUID> roles;
    private List<UUID> authorities;
}
