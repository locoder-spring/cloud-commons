package icu.lowcoder.spring.cloud.config.db.dao;

import icu.lowcoder.spring.cloud.config.db.entity.Properties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface PropertiesRepository extends JpaRepository<Properties, UUID>, JpaSpecificationExecutor<Properties> {
}
