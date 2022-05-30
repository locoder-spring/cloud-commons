package icu.lowcoder.spring.cloud.organization.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class DepartmentListItem {
    private UUID id;
    private String name;
    private UUID parentId;
    private String parentName;
}
