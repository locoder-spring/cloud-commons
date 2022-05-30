package icu.lowcoder.spring.cloud.organization.dao;

import icu.lowcoder.spring.cloud.organization.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {

    boolean existsByPhone(String phone);

    Optional<Employee> findOneByAccountId(UUID accountId);
}
