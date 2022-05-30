package icu.lowcoder.spring.cloud.organization.service.management;

import icu.lowcoder.spring.cloud.organization.dto.ManagementUserModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/management/users")
public interface ManagementUsersService {

    @GetMapping(params = "principal")
    ManagementUserModel principal();
}
