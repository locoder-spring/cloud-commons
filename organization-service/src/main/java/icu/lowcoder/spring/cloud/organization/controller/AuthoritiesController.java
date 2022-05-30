package icu.lowcoder.spring.cloud.organization.controller;

import icu.lowcoder.spring.commons.util.spring.BeanUtils;
import icu.lowcoder.spring.cloud.organization.dao.AuthorityRepository;
import icu.lowcoder.spring.cloud.organization.dto.request.AddAuthorityRequest;
import icu.lowcoder.spring.cloud.organization.dto.request.EditAuthorityRequest;
import icu.lowcoder.spring.cloud.organization.dto.request.ListAuthoritiesParams;
import icu.lowcoder.spring.cloud.organization.dto.response.AuthorityResponse;
import icu.lowcoder.spring.cloud.organization.dto.response.UUIDIdResponse;
import icu.lowcoder.spring.cloud.organization.entity.Authority;
import icu.lowcoder.spring.cloud.organization.entity.Employee;
import icu.lowcoder.spring.cloud.organization.manager.CacheableEmployeeAuthoritiesManager;
import icu.lowcoder.spring.cloud.organization.service.AuthoritiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

import javax.persistence.criteria.CriteriaBuilder;
import javax.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class AuthoritiesController implements AuthoritiesService {

    @Autowired
    private CacheableEmployeeAuthoritiesManager employeeAuthoritiesManager;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('org_authorities_add') or hasRole('SYSTEM_MANAGER')")
    public UUIDIdResponse add(@Valid @RequestBody AddAuthorityRequest request) {
        // code to lower case
        request.setCode(request.getCode().toLowerCase());

        // CODE 存在判断
        if (authorityRepository.existsByCode(request.getCode())) {
            throw new HttpClientErrorException(HttpStatus.PRECONDITION_FAILED, request.getCode() + " 已存在");
        }

        Authority authority = BeanUtils.instantiate(Authority.class, request);
        authorityRepository.save(authority);

        return new UUIDIdResponse(authority.getId());
    }

    @Override
    @PreAuthorize("hasAuthority('org_authorities_list') or hasRole('SYSTEM_MANAGER')")
    public Page<AuthorityResponse> page(ListAuthoritiesParams params, @PageableDefault(sort = {"createdTime", "id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        return authorityRepository.findAll(Specification.where((root, query, cb) -> {
                    if (params.getRoleIds() != null && !params.getRoleIds().isEmpty()) {
                        CriteriaBuilder.In<UUID> rolesIdIn = cb.in(root.join("roles").get("id"));
                        for (UUID roleId : params.getRoleIds()) {
                            rolesIdIn.value(roleId);
                        }
                        query.distinct(true);
                        return rolesIdIn;
                    }
                    return null;
                }), pageable)
                .map(authority -> BeanUtils.instantiate(AuthorityResponse.class, authority));
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('org_authorities_edit') or hasRole('SYSTEM_MANAGER')")
    public void edit(@PathVariable UUID id, @Valid @RequestBody EditAuthorityRequest request) {
        Authority authority = getAuthority(id);
        if (authority.getBuiltIn()) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "内置权限无法修改");
        }

        BeanUtils.copyProperties(request, authority);
    }

    @Override
    @PreAuthorize("hasAuthority('org_authorities_get') or hasRole('SYSTEM_MANAGER')")
    public AuthorityResponse get(@PathVariable UUID id) {
        Authority authority = getAuthority(id);

        return BeanUtils.instantiate(AuthorityResponse.class, authority);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('org_authorities_del') or hasRole('SYSTEM_MANAGER')")
    public void del(@PathVariable UUID id) {
        Authority authority = getAuthority(id);
        if (authority.getBuiltIn()) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "内置权限无法删除");
        }
        // 记下准备缓存
        List<UUID> employeeAccountIds = authority.getEmployees().stream().map(Employee::getAccountId).collect(Collectors.toList());

        authorityRepository.delete(authority);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                employeeAccountIds.forEach(accountId -> employeeAuthoritiesManager.clearEmployeeAuthoritiesCache(accountId));
            }
        });
    }

    private Authority getAuthority(UUID id) {
        return authorityRepository.findById(id)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "权限不存在"));
    }

}
