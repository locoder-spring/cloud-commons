package icu.lowcoder.spring.cloud.organization.controller;

import icu.lowcoder.spring.commons.jpa.CommonEntity;
import icu.lowcoder.spring.commons.util.spring.BeanUtils;
import icu.lowcoder.spring.cloud.organization.dao.AuthorityRepository;
import icu.lowcoder.spring.cloud.organization.dao.RoleRepository;
import icu.lowcoder.spring.cloud.organization.dto.request.AddRoleRequest;
import icu.lowcoder.spring.cloud.organization.dto.request.DelRoleOptions;
import icu.lowcoder.spring.cloud.organization.dto.request.EditRoleOptions;
import icu.lowcoder.spring.cloud.organization.dto.request.EditRoleRequest;
import icu.lowcoder.spring.cloud.organization.dto.response.RoleResponse;
import icu.lowcoder.spring.cloud.organization.dto.response.UUIDIdResponse;
import icu.lowcoder.spring.cloud.organization.entity.Authority;
import icu.lowcoder.spring.cloud.organization.entity.Employee;
import icu.lowcoder.spring.cloud.organization.entity.Role;
import icu.lowcoder.spring.cloud.organization.manager.CacheableEmployeeAuthoritiesManager;
import icu.lowcoder.spring.cloud.organization.service.RolesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class RolesController implements RolesService {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private AuthorityRepository authorityRepository;
    @Autowired
    private CacheableEmployeeAuthoritiesManager employeeAuthoritiesManager;

    private String rolePrefix = "ROLE_";

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('org_roles_add') or hasRole('SYSTEM_MANAGER')")
    public UUIDIdResponse add(@Valid @RequestBody AddRoleRequest request) {
        // code to upper case
        request.setCode(completeRoleCode(rolePrefix, request.getCode()));

        // CODE 存在判断
        if (roleRepository.existsByCode(request.getCode())) {
            throw new HttpClientErrorException(HttpStatus.PRECONDITION_FAILED, request.getCode() + " 已存在");
        }

        Role role = BeanUtils.instantiate(Role.class, request, "authorities");
        // 权限处理
        List<Authority> requestAuthorities = new ArrayList<>();
        if (request.getAuthorities() != null && !request.getAuthorities().isEmpty()) {
            requestAuthorities.addAll(authorityRepository.findAllById(request.getAuthorities()));
        }
        if (!requestAuthorities.isEmpty()) {
            role.getAuthorities().addAll(requestAuthorities);
        }

        roleRepository.save(role);

        return new UUIDIdResponse(role.getId());
    }

    @Override
    @PreAuthorize("hasAuthority('org_roles_list') or hasRole('SYSTEM_MANAGER')")
    public Page<RoleResponse> page(@PageableDefault(sort = "createdTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return roleRepository.findAll(pageable)
                .map(role -> {
                    RoleResponse roleModel = BeanUtils.instantiate(RoleResponse.class, role, "authorities");
                    roleModel.setAuthorities(role.getAuthorities().stream().map(CommonEntity::getId).collect(Collectors.toList()));
                    return roleModel;
                });
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('org_roles_edit') or hasRole('SYSTEM_MANAGER')")
    public void edit(@PathVariable UUID id, @Valid @RequestBody EditRoleRequest request, EditRoleOptions options) {
        Role role = getRole(id);
        if (role.getBuiltIn()) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "内置角色无法修改");
        }

        BeanUtils.copyProperties(request, role, "authorities");

        // 新的权限列表
        List<Authority> requestAuthorities = new ArrayList<>();
        if (request.getAuthorities() != null && !request.getAuthorities().isEmpty()) {
            requestAuthorities.addAll(authorityRepository.findAllById(request.getAuthorities()));
        }
        role.getAuthorities().clear();
        role.getAuthorities().addAll(requestAuthorities);

        // 同步更改授权
        if (options.isUpdateAuthorizations()) {
            List<UUID> employeeAccountIds = role.getEmployees().stream().map(Employee::getAccountId).collect(Collectors.toList());

            role.getEmployees().forEach(e -> {
                List<Authority> needRemove = e.getAuthorities().stream()
                        .filter(oa ->
                                requestAuthorities.stream().noneMatch(ra -> ra.getId().equals(oa.getId())) &&
                                oa.getRoles().stream().filter(ar -> e.getRoles().stream().anyMatch(er -> er.getId().equals(ar.getId()))).count() <= 1
                        ) // 即将删除且其他角色也不包含该权限
                        .collect(Collectors.toList());

                List<Authority> needAdd = requestAuthorities.stream()
                        .filter(ra -> e.getAuthorities().stream().noneMatch(oa -> oa.getId().equals(ra.getId()))) // 没有新增的权限
                        .collect(Collectors.toList());

                // remove
                e.getAuthorities().removeIf(or -> needRemove.stream().anyMatch(rr -> rr.getId().equals(or.getId())));
                // add
                e.getAuthorities().addAll(needAdd);
            });

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    employeeAccountIds.forEach(accountId -> employeeAuthoritiesManager.clearEmployeeAuthoritiesCache(accountId));
                }
            });
        }

    }

    @Override
    @PreAuthorize("hasAuthority('org_roles_get') or hasRole('SYSTEM_MANAGER')")
    public RoleResponse get(@PathVariable UUID id) {
        Role role = getRole(id);

        RoleResponse roleModel = BeanUtils.instantiate(RoleResponse.class, role, "authorities");
        roleModel.setAuthorities(role.getAuthorities().stream().map(CommonEntity::getId).collect(Collectors.toList()));
        return roleModel;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('org_roles_del') or hasRole('SYSTEM_MANAGER')")
    public void del(@PathVariable UUID id, DelRoleOptions options) {
        Role role = getRole(id);
        if (role.getBuiltIn()) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "内置角色无法删除");
        }

        // 同步更改授权
        if (options.isUpdateAuthorizations()) {
            List<UUID> employeeAccountIds = role.getEmployees().stream().map(Employee::getAccountId).collect(Collectors.toList());

            role.getEmployees().forEach(e -> {
                List<Authority> needRemove = e.getAuthorities().stream()
                        .filter(oa -> role.getAuthorities().stream().anyMatch(ra -> ra.getId().equals(oa.getId())) &&
                                      oa.getRoles().stream().filter(ar -> e.getRoles().stream().anyMatch(er -> er.getId().equals(ar.getId()))).count() <= 1
                        ) // 即将删除且其他角色也不包含该权限
                        .collect(Collectors.toList());

                // remove
                e.getAuthorities().removeIf(or -> needRemove.stream().anyMatch(rr -> rr.getId().equals(or.getId())));
            });

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    employeeAccountIds.forEach(accountId -> employeeAuthoritiesManager.clearEmployeeAuthoritiesCache(accountId));
                }
            });
        }

        roleRepository.delete(role);
    }

    private Role getRole(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "角色不存在"));
    }

    private String completeRoleCode(String prefix, String code) {
        if (code.startsWith(prefix)) {
            return code.toUpperCase();
        } else {
            return (prefix + code).toUpperCase();
        }
    }
}
