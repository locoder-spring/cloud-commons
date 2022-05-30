package icu.lowcoder.spring.cloud.organization.dao;

import icu.lowcoder.spring.cloud.organization.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    List<Department> findAllByParentIsNull();

    List<Department> findAllByParentId(UUID parentId);
}
