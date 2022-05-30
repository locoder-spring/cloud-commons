package icu.lowcoder.spring.cloud.message.feign.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class Account {
    private UUID id;
    private String name;
    private String phone;
    private String email;
    private String qq;
    private Date registerTime;
    private Boolean enabled;
}
