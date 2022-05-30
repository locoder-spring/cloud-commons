package icu.lowcoder.spring.cloud.organization.feign.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountRegisterRequest {
    private String phone;
    private String email;
    private String name;
    private String qq;
    private String password;
}
