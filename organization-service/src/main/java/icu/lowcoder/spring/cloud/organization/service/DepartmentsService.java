package icu.lowcoder.spring.cloud.organization.service;

import icu.lowcoder.spring.cloud.organization.dto.request.AddDepartmentRequest;
import icu.lowcoder.spring.cloud.organization.dto.request.DepartmentTreeParams;
import icu.lowcoder.spring.cloud.organization.dto.request.EditDepartmentRequest;
import icu.lowcoder.spring.cloud.organization.dto.response.DepartmentListItem;
import icu.lowcoder.spring.cloud.organization.dto.response.DepartmentTreeNode;
import icu.lowcoder.spring.cloud.organization.dto.response.UUIDIdResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/departments")
public interface DepartmentsService {

    @GetMapping(params = "tree")
    List<DepartmentTreeNode> tree(DepartmentTreeParams params);

    @GetMapping(params = "all")
    List<DepartmentListItem> all();

    @PostMapping
    UUIDIdResponse add(AddDepartmentRequest request);

    @PutMapping("/{departmentId}")
    void edit(@PathVariable UUID departmentId, EditDepartmentRequest request);

    @DeleteMapping("/{departmentId}")
    void del(@PathVariable UUID departmentId);
}
