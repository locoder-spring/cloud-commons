package icu.lowcoder.spring.cloud.organization.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ListEmployeesParams {
    private UUID departmentId;

    private String keyword;

    private List<String> positionCodes;

    private List<UUID> ids;
}
