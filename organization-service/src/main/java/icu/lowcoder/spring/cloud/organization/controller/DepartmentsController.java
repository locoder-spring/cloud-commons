package icu.lowcoder.spring.cloud.organization.controller;

import icu.lowcoder.spring.commons.util.spring.BeanUtils;
import icu.lowcoder.spring.cloud.organization.dao.DepartmentRepository;
import icu.lowcoder.spring.cloud.organization.dto.request.AddDepartmentRequest;
import icu.lowcoder.spring.cloud.organization.dto.request.DepartmentTreeParams;
import icu.lowcoder.spring.cloud.organization.dto.request.EditDepartmentRequest;
import icu.lowcoder.spring.cloud.organization.dto.response.DepartmentListItem;
import icu.lowcoder.spring.cloud.organization.dto.response.DepartmentTreeNode;
import icu.lowcoder.spring.cloud.organization.dto.response.UUIDIdResponse;
import icu.lowcoder.spring.cloud.organization.entity.Department;
import icu.lowcoder.spring.cloud.organization.service.DepartmentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class DepartmentsController implements DepartmentsService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    @PreAuthorize("hasAuthority('org_departments_list') or hasRole('SYSTEM_MANAGER')")
    public List<DepartmentTreeNode> tree(DepartmentTreeParams params) {
        List<Department> rootDepartments;
        if (params.getParentId() != null) {
            rootDepartments = departmentRepository.findAllByParentId(params.getParentId());
        } else {
            rootDepartments = departmentRepository.findAllByParentIsNull();
        }
        return rootDepartments.stream().map(this::mapToTreeNode).collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasAuthority('org_departments_list') or hasRole('SYSTEM_MANAGER')")
    public List<DepartmentListItem> all() {
        List<Department> departments = departmentRepository.findAll();
        return departments.stream().map(dept -> {
            DepartmentListItem item = BeanUtils.instantiate(DepartmentListItem.class, dept, "parent", "children");
            if (dept.getParent() != null) {
                item.setParentId(dept.getParent().getId());
                item.setParentName(dept.getParent().getName());
            }
            return item;
        }).collect(Collectors.toList());
    }

    private DepartmentTreeNode mapToTreeNode(Department department) {
        DepartmentTreeNode node = BeanUtils.instantiate(DepartmentTreeNode.class, department, "employees", "parent", "children");
        if (!department.getChildren().isEmpty()) {
            node.setChildren(department.getChildren().stream().map(this::mapToTreeNode).collect(Collectors.toList()));
        }

        return node;
    }


    @Override
    @Transactional
    @PreAuthorize("hasAuthority('org_departments_add') or hasRole('SYSTEM_MANAGER')")
    public UUIDIdResponse add(@Valid @RequestBody AddDepartmentRequest request) {
        Department department = BeanUtils.instantiate(Department.class, request);

        // parent
        if (request.getParentId() != null) {
            Department parent = departmentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "上级部门不存在"));

            department.setParent(parent);
        }

        departmentRepository.save(department);

        return new UUIDIdResponse(department.getId());
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('org_departments_edit') or hasRole('SYSTEM_MANAGER')")
    public void edit(@PathVariable UUID departmentId, @Valid @RequestBody EditDepartmentRequest request) {
        Department department = getDepartment(departmentId);

        // parent
        if (request.getParentId() != null) {
            Department parent = departmentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "上级部门不存在"));

            department.setParent(parent);
        }

        department.setName(request.getName());
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('org_departments_del') or hasRole('SYSTEM_MANAGER')")
    public void del(@PathVariable UUID departmentId) {
        Department department = getDepartment(departmentId);

        departmentRepository.delete(department);
    }

    private Department getDepartment(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "部门不存在"));
    }
}
