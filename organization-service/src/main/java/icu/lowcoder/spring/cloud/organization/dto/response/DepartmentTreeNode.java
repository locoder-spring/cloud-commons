package icu.lowcoder.spring.cloud.organization.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class DepartmentTreeNode {
    private UUID id;
    private String name;
    private List<DepartmentTreeNode> children = new ArrayList<>();
}
