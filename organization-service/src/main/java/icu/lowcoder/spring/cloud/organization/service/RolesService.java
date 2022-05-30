package icu.lowcoder.spring.cloud.organization.service;

import icu.lowcoder.spring.cloud.organization.dto.request.*;
import icu.lowcoder.spring.cloud.organization.dto.response.RoleResponse;
import icu.lowcoder.spring.cloud.organization.dto.response.UUIDIdResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/roles")
public interface RolesService {

    @PostMapping
    UUIDIdResponse add(AddRoleRequest request);

    @GetMapping
    Page<RoleResponse> page(Pageable pageable);

    @PutMapping("/{id}")
    void edit(@PathVariable UUID id, EditRoleRequest request, EditRoleOptions options);

    @GetMapping("/{id}")
    RoleResponse get(@PathVariable UUID id);

    @DeleteMapping("/{id}")
    void del(@PathVariable UUID id, DelRoleOptions options);

}
