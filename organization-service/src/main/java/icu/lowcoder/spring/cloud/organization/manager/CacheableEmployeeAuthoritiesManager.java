package icu.lowcoder.spring.cloud.organization.manager;

import icu.lowcoder.spring.commons.util.spring.BeanUtils;
import icu.lowcoder.spring.cloud.organization.dao.EmployeeRepository;
import icu.lowcoder.spring.cloud.organization.dto.AuthorityModel;
import icu.lowcoder.spring.cloud.organization.dto.RoleModel;
import icu.lowcoder.spring.cloud.organization.entity.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@CacheConfig(cacheNames = "icu.lowcoder.spring.cloud.org.employee")
public class CacheableEmployeeAuthoritiesManager {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Cacheable(key = "'roles:' + #accountId.toString()")
    @Transactional(readOnly = true)
    public List<RoleModel> getEmployeeRoles(UUID accountId) {
        Optional<Employee> employee = employeeRepository.findOneByAccountId(accountId);
        employee.orElseThrow(() -> new RuntimeException("员工不存在"));

        return employee.get().getRoles()
                .stream()
                .map(role -> BeanUtils.instantiate(RoleModel.class, role))
                .collect(Collectors.toList());
    }

    @CacheEvict(key = "'roles:' + #accountId.toString()", beforeInvocation = true)
    public void clearEmployeeRolesCache(UUID accountId) {
    }

    @Cacheable(key = "'authorities:' + #accountId.toString()")
    @Transactional(readOnly = true)
    public List<AuthorityModel> getEmployeeAuthorities(UUID accountId) {
        Optional<Employee> employee = employeeRepository.findOneByAccountId(accountId);
        employee.orElseThrow(() -> new RuntimeException("员工不存在"));

        return employee.get().getAuthorities()
                .stream()
                .map(authority -> BeanUtils.instantiate(AuthorityModel.class, authority))
                .collect(Collectors.toList());
    }

    @CacheEvict(key = "'authorities:' + #accountId.toString()", beforeInvocation = true)
    public void clearEmployeeAuthoritiesCache(UUID accountId) {
    }

}
