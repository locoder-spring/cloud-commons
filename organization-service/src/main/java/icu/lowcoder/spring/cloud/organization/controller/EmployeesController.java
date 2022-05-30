package icu.lowcoder.spring.cloud.organization.controller;

import icu.lowcoder.spring.commons.jpa.CommonEntity;
import icu.lowcoder.spring.commons.util.spring.BeanUtils;
import icu.lowcoder.spring.cloud.organization.dao.AuthorityRepository;
import icu.lowcoder.spring.cloud.organization.dao.DepartmentRepository;
import icu.lowcoder.spring.cloud.organization.dao.EmployeeRepository;
import icu.lowcoder.spring.cloud.organization.dao.RoleRepository;
import icu.lowcoder.spring.cloud.organization.dao.spce.EmployeeSpecs;
import icu.lowcoder.spring.cloud.organization.dto.AuthorityModel;
import icu.lowcoder.spring.cloud.organization.dto.RoleModel;
import icu.lowcoder.spring.cloud.organization.dto.request.AddEmployeeRequest;
import icu.lowcoder.spring.cloud.organization.dto.request.EditEmployeeRequest;
import icu.lowcoder.spring.cloud.organization.dto.request.GetEmployeeParams;
import icu.lowcoder.spring.cloud.organization.dto.request.ListEmployeesParams;
import icu.lowcoder.spring.cloud.organization.dto.response.EmployeeAuthoritiesDetail;
import icu.lowcoder.spring.cloud.organization.dto.response.EmployeeDetail;
import icu.lowcoder.spring.cloud.organization.dto.response.EmployeeListItem;
import icu.lowcoder.spring.cloud.organization.dto.response.UUIDIdResponse;
import icu.lowcoder.spring.cloud.organization.entity.Authority;
import icu.lowcoder.spring.cloud.organization.entity.Department;
import icu.lowcoder.spring.cloud.organization.entity.Employee;
import icu.lowcoder.spring.cloud.organization.entity.Role;
import icu.lowcoder.spring.cloud.organization.feign.CommonsAuthenticationAccountsClient;
import icu.lowcoder.spring.cloud.organization.feign.model.AccountDetail;
import icu.lowcoder.spring.cloud.organization.feign.model.AccountRegisterRequest;
import icu.lowcoder.spring.cloud.organization.manager.CacheableEmployeeAuthoritiesManager;
import icu.lowcoder.spring.cloud.organization.manager.PositionManager;
import icu.lowcoder.spring.cloud.organization.service.EmployeesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class EmployeesController implements EmployeesService {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private CommonsAuthenticationAccountsClient commonsAuthenticationAccountsClient;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private AuthorityRepository authorityRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private CacheableEmployeeAuthoritiesManager employeeAuthoritiesManager;
    @Autowired
    private PositionManager positionManager;

    @Override
    @PreAuthorize("(#oauth2.client and #oauth2.clientHasRole('ROLE_SERVICE_CLIENT')) or (#oauth2.user and (hasAuthority('org_employees_list') or hasRole('SYSTEM_MANAGER'))) ")
    public Page<EmployeeListItem> page(@Valid ListEmployeesParams params, @PageableDefault(sort = "createdTime", direction = Sort.Direction.DESC) Pageable pageable) {
        List<UUID> authoritiesIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(params.getPositionCodes())) {
            authoritiesIds.addAll(positionManager.positionsAuthorities(params.getPositionCodes().toArray(new String[0])));
            // 如果按职位查询，但权限列表返回空，则员工返回空
            if (authoritiesIds.isEmpty()) {
                return new PageImpl<>(Collections.emptyList());
            }
        }

        return employeeRepository.findAll(
                Specification.<Employee>where(EmployeeSpecs.inDepartment(params.getDepartmentId()))
                        .and(EmployeeSpecs.keywordMatch(params.getKeyword()))
                        .and(EmployeeSpecs.authoritiesIdAllMatch(authoritiesIds))
                        .and(EmployeeSpecs.idIn(params.getIds()))
                , pageable
        ).map(e -> {
            EmployeeListItem item = BeanUtils.instantiate(EmployeeListItem.class, e, "departments");
            item.setDepartments(e.getDepartments().stream().map(Department::getId).collect(Collectors.toList()));
            return item;
        });
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('org_employees_add') or hasRole('SYSTEM_MANAGER')")
    public UUIDIdResponse add(@Valid @RequestBody AddEmployeeRequest request) {
        // 手机号是否存在
        if (employeeRepository.existsByPhone(request.getPhone())) {
            throw new HttpClientErrorException(HttpStatus.PRECONDITION_FAILED, request.getPhone() + " 已存在");
        }
        Employee employee = BeanUtils.instantiate(Employee.class, request, "authorities", "departments", "roles");

        AccountDetail accountDetail = commonsAuthenticationAccountsClient.getByPhone(request.getPhone());
        if (accountDetail == null) {
            AccountRegisterRequest registerRequest = BeanUtils.instantiate(AccountRegisterRequest.class, request);
            UUIDIdResponse idResponse = commonsAuthenticationAccountsClient.register(registerRequest);

            employee.setAccountId(idResponse.getId());
        } else {
            employee.setAccountId(accountDetail.getId());
        }

        // 处理部门
        setDepartments(employee, request.getDepartments());

        // 处理角色
        setRoles(employee, request.getRoles());

        // 处理权限
        setAuthorities(employee, request.getAuthorities());

        employeeRepository.save(employee);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                employeeAuthoritiesManager.clearEmployeeAuthoritiesCache(employee.getAccountId());
                employeeAuthoritiesManager.clearEmployeeRolesCache(employee.getAccountId());
            }
        });

        return new UUIDIdResponse(employee.getId());
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('org_employees_edit') or hasRole('SYSTEM_MANAGER')")
    public void edit(@PathVariable UUID employeeId, @Valid @RequestBody EditEmployeeRequest request) {
        Employee employee = getEmployee(employeeId);

        BeanUtils.copyProperties(request, employee, "authorities", "departments", "roles");

        // 处理部门
        setDepartments(employee, request.getDepartments());

        // 处理角色
        setRoles(employee, request.getRoles());

        // 处理权限
        setAuthorities(employee, request.getAuthorities());

        employeeRepository.save(employee);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                employeeAuthoritiesManager.clearEmployeeAuthoritiesCache(employee.getAccountId());
                employeeAuthoritiesManager.clearEmployeeRolesCache(employee.getAccountId());
            }
        });
    }

    @Override
    @PreAuthorize("(#oauth2.client and #oauth2.clientHasRole('ROLE_SERVICE_CLIENT')) or (#oauth2.user and (hasAuthority('org_employees_get') or hasRole('SYSTEM_MANAGER'))) ")
    public EmployeeListItem getEmployee(@Valid GetEmployeeParams params) {
        List<Authority> authorities = authorityRepository
                .findAllByPositionsCodeIn(params.getPositionCodes().stream().map(String::toUpperCase).collect(Collectors.toList()));
        if (authorities.isEmpty()) {
            return null;
        }
        List<UUID> authoritiesIds = authorities.stream().map(CommonEntity::getId).collect(Collectors.toList());
        Optional<Employee> one = employeeRepository.findOne(Specification
                .<Employee>where((root, query, cb) -> cb.and(cb.equal(root.get("id"), params.getEmployeeId())))
                .and((root, query, cb) -> {
                    Subquery<UUID> authoritiesIdsQuery = query.subquery(UUID.class);
                    Root<Employee> employeeRoot = authoritiesIdsQuery.from(Employee.class);
                    authoritiesIdsQuery.select(employeeRoot.join("authorities", JoinType.LEFT).get("id"));
                    authoritiesIdsQuery.where(cb.equal(root, employeeRoot));
                    Predicate[] andPredicates = authoritiesIds.stream()
                            .map(authorityId -> {
                                Subquery<UUID> idQuery = query.subquery(UUID.class);
                                Root<Authority> authorityRoot = idQuery.from(Authority.class);
                                idQuery.select(authorityRoot.get("id"));
                                idQuery.where(cb.equal(authorityRoot.get("id"), authorityId));
                                return cb.in(idQuery).value(authoritiesIdsQuery);
                            })
                            .toArray(Predicate[]::new);
                    return cb.and(andPredicates);
                }));
        EmployeeListItem employeeListItem = new EmployeeListItem();
        one.ifPresent(employee -> BeanUtils.copyProperties(employee, employeeListItem));
        return null;
    }

    private Employee setDepartments(Employee employee, List<UUID> ids) {
        List<Department> departments = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            departments.addAll(departmentRepository.findAllById(ids));
        }

        employee.getDepartments().clear();
        if (!departments.isEmpty()) {
            employee.getDepartments().addAll(departments);
        }

        return employee;
    }

    private Employee setRoles(Employee employee, List<UUID> ids) {
        List<Role> roles = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            // 内置角色无法授予，只能通过account.authorities授予。
            // 在org服务内创建的意义在于占位，避免创建出相同的角色
            roles.addAll(
                    roleRepository.findAllById(ids)
                            .stream()
                            .filter(role -> !role.getBuiltIn()).collect(Collectors.toList())
            );
        }

        employee.getRoles().clear();
        if (!roles.isEmpty()) {
            employee.getRoles().addAll(roles);
        }

        return employee;
    }

    private Employee setAuthorities(Employee employee, List<UUID> ids) {
        List<Authority> authorities = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            authorities.addAll(authorityRepository.findAllById(ids));
        }

        employee.getAuthorities().clear();
        if (!authorities.isEmpty()) {
            employee.getAuthorities().addAll(authorities);
        }

        return employee;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('org_employees_del') or hasRole('SYSTEM_MANAGER')")
    public void del(@PathVariable UUID employeeId) {
        Employee employee = getEmployee(employeeId);

        UUID accountId = employee.getAccountId();

        employeeRepository.delete(employee);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                employeeAuthoritiesManager.clearEmployeeAuthoritiesCache(accountId);
                employeeAuthoritiesManager.clearEmployeeRolesCache(accountId);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('org_employees_get') or hasRole('SYSTEM_MANAGER')")
    public EmployeeDetail get(@PathVariable UUID employeeId) {
        Employee employee = getEmployee(employeeId);

        EmployeeDetail detail = BeanUtils.instantiate(EmployeeDetail.class, employee, "authorities", "departments", "roles");

        detail.setAuthorities(employee.getAuthorities().stream().map(Authority::getId).collect(Collectors.toList()));
        detail.setDepartments(employee.getDepartments().stream().map(Department::getId).collect(Collectors.toList()));
        detail.setRoles(employee.getRoles().stream().map(Role::getId).collect(Collectors.toList()));

        return detail;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('org_employees_get') or hasRole('SYSTEM_MANAGER')")
    public EmployeeDetail getByIdAndPosition(UUID employeeId, String positionCode) {
        List<UUID> authoritiesIds = new ArrayList<>();
        if (StringUtils.hasText(positionCode)) {
            authoritiesIds.addAll(positionManager.positionsAuthorities(positionCode));

            // 如果按职位查询，但权限列表返回空，则员工返回空
            if (authoritiesIds.isEmpty()) {
                return null;
            }
        }

        Optional<Employee> employee = employeeRepository.findOne(Specification
                .<Employee>where((root, query, cb) -> cb.equal(root.get("id"), employeeId))
                .and(EmployeeSpecs.authoritiesIdAllMatch(authoritiesIds))
        );

        return employee
                .map(e -> BeanUtils.instantiate(EmployeeDetail.class, employee, "authorities", "departments", "roles"))
                .orElse(null);
    }

    @Override
    @PreAuthorize("(#oauth2.client and #oauth2.clientHasRole('ROLE_SERVICE_CLIENT')) or (#oauth2.user and #accountId.equals(authentication.principal.id)) ")
    public EmployeeAuthoritiesDetail getByAccountId(UUID accountId) {

        return employeeRepository.findOneByAccountId(accountId)
                .map(employee -> {
                    EmployeeAuthoritiesDetail detail = BeanUtils.instantiate(EmployeeAuthoritiesDetail.class, employee, "authorities");
                    detail.getAuthorities().addAll(
                            employeeAuthoritiesManager.getEmployeeAuthorities(employee.getAccountId()).stream()
                                    .map(AuthorityModel::getCode)
                                    .distinct()
                                    .collect(Collectors.toList())
                    );
                    detail.getAuthorities().addAll(
                            employeeAuthoritiesManager.getEmployeeRoles(employee.getAccountId()).stream()
                                    .map(RoleModel::getCode)
                                    .distinct()
                                    .collect(Collectors.toList())
                    );

                    return detail;
                })
                .orElse(null);
    }

    @Override
    @PreAuthorize("(#oauth2.client and #oauth2.clientHasRole('ROLE_SERVICE_CLIENT')) or (#oauth2.user and (hasAuthority('org_employees_get') or hasRole('SYSTEM_MANAGER'))) ")
    public EmployeeAuthoritiesDetail getByEmployeeId(UUID employeeId) {
        return employeeRepository.findById(employeeId)
                .map(employee -> {
                    EmployeeAuthoritiesDetail detail = BeanUtils.instantiate(EmployeeAuthoritiesDetail.class, employee, "authorities");
                    detail.getAuthorities().addAll(
                            employeeAuthoritiesManager.getEmployeeAuthorities(employee.getAccountId()).stream()
                                    .map(AuthorityModel::getCode)
                                    .distinct()
                                    .collect(Collectors.toList())
                    );
                    detail.getAuthorities().addAll(
                            employeeAuthoritiesManager.getEmployeeRoles(employee.getAccountId()).stream()
                                    .map(RoleModel::getCode)
                                    .distinct()
                                    .collect(Collectors.toList())
                    );

                    return detail;
                })
                .orElse(null);
    }

    @Override
    @PreAuthorize("(#oauth2.client and #oauth2.clientHasRole('ROLE_SERVICE_CLIENT')) or (#oauth2.user and (hasAuthority('org_employees_get') or hasRole('SYSTEM_MANAGER'))) ")
    public List<EmployeeListItem> listByAuthorityCode(@PathVariable String authorityCode) {
        Authority authority = authorityRepository.findByCode(authorityCode);
        if (null == authority) {
            return Collections.emptyList();
        }
        return authority.getEmployees().stream().map(entity -> {
            EmployeeListItem item = new EmployeeListItem();
            item.setAccountId(entity.getAccountId());
            item.setEmail(entity.getEmail());
            item.setId(entity.getId());
            item.setName(entity.getName());
            item.setNo(entity.getNo());
            item.setPhone(entity.getPhone());
            return item;
        }).collect(Collectors.toList());
    }

    private Employee getEmployee(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "员工不存在"));
    }
}
