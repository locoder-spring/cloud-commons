package icu.lowcoder.spring.cloud.organization.dao;

import icu.lowcoder.spring.cloud.organization.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PositionRepository extends JpaRepository<Position, UUID> {

    boolean existsByCode(String code);
}
