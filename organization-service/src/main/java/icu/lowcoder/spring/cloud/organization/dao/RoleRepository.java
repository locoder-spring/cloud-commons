package icu.lowcoder.spring.cloud.organization.dao;

import icu.lowcoder.spring.cloud.organization.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    boolean existsByCode(String code);
}
