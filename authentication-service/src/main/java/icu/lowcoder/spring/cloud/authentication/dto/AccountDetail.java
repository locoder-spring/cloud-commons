package icu.lowcoder.spring.cloud.authentication.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class AccountDetail {
    private UUID id;
    private String name;
    private String phone;
    private String email;
    private String qq;
    private Date registerTime;
    private Boolean enabled;
}
