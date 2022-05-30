package icu.lowcoder.spring.cloud.authentication.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ManagementAccountsResponse {
    private UUID id;
    private String email;
    private Boolean enabled;
    private String name;
    private String phone;
    private String qq;
    private Date registerTime;
    private List<String> authorities;
}
