package icu.lowcoder.spring.cloud.authentication.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class UpdateRequest {
    @Email
    private String email;
    private String name;
    private String qq;
    private String password;
}
