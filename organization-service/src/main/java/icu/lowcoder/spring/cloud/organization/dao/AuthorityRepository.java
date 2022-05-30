package icu.lowcoder.spring.cloud.organization.dao;

import icu.lowcoder.spring.cloud.organization.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface AuthorityRepository extends JpaRepository<Authority, UUID>, JpaSpecificationExecutor<Authority> {

    boolean existsByCode(String code);

    List<Authority> findAllByPositionsCodeIn(List<String> positionCodes);

    Authority findByCode(String code);

}
