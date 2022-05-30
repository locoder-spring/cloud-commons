package icu.lowcoder.spring.cloud.organization.controller.management;

import icu.lowcoder.spring.commons.security.AccountModel;
import icu.lowcoder.spring.commons.security.SecurityUtils;
import icu.lowcoder.spring.commons.util.spring.BeanUtils;
import icu.lowcoder.spring.cloud.organization.dao.EmployeeRepository;
import icu.lowcoder.spring.cloud.organization.dto.ManagementUserModel;
import icu.lowcoder.spring.cloud.organization.entity.Authority;
import icu.lowcoder.spring.cloud.organization.entity.Employee;
import icu.lowcoder.spring.cloud.organization.entity.Role;
import icu.lowcoder.spring.cloud.organization.service.management.ManagementUsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ManagementUsersController implements ManagementUsersService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    @PreAuthorize("#oauth2.user")
    public ManagementUserModel principal() {
        // account
        AccountModel accountModel = SecurityUtils.getPrincipal(AccountModel.class);

        // employee info
        Employee employee = employeeRepository.findOneByAccountId(accountModel.getId())
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "未认证"));

        ManagementUserModel userModel = new ManagementUserModel();
        BeanUtils.copyProperties(accountModel, userModel, "authorities");
        BeanUtils.copyProperties(employee, userModel, "departments", "roles", "authorities", "id");

        List<String> accountAuthorities = accountModel.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // account authorities
        userModel.getAuthorities().addAll(
                accountAuthorities.stream()
                        .filter(authority -> !authority.startsWith("ROLE_"))
                        .distinct()
                        .collect(Collectors.toList())
        );
        userModel.getRoles().addAll(
                accountAuthorities.stream()
                        .filter(authority -> authority.startsWith("ROLE_"))
                        .distinct()
                        .collect(Collectors.toList())
        );

        // employee authorities
        userModel.setEmployeeId(employee.getId());
        userModel.getAuthorities().addAll(
                employee.getAuthorities().stream()
                        .map(Authority::getCode)
                        .distinct()
                        .collect(Collectors.toList())
        );
        userModel.getRoles().addAll(
                employee.getRoles().stream()
                        .map(Role::getCode)
                        .distinct()
                        .collect(Collectors.toList())
        );

        SecurityUtils.getAuthorities().forEach(authority -> {
            if (authority.startsWith("ROLE_") && !userModel.getRoles().contains(authority)) {
                userModel.getRoles().add(authority);
            } else if(!authority.startsWith("ROLE_") && !userModel.getAuthorities().contains(authority))
            if (authority.startsWith("ROLE_") && !userModel.getRoles().contains(authority)) {
                userModel.getAuthorities().add(authority);
            }
        });

        return userModel;
    }
}
