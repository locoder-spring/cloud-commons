package icu.lowcoder.spring.cloud.authentication.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class AddAccountRequest {
    private String email;
    private Boolean enabled = true;
    @NotBlank
    private String name;
    @NotBlank
    private String phone;
    private String qq;
    private Set<String> authorities = new HashSet<>();
}
