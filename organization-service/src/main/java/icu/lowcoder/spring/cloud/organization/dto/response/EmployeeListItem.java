package icu.lowcoder.spring.cloud.organization.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class EmployeeListItem {
    private UUID id;
    private String name;
    private String phone;
    private String email;
    private String no;
    private UUID accountId;

    private List<UUID> departments;
}
