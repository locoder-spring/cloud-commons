package icu.lowcoder.spring.cloud.authentication.dao;

import icu.lowcoder.spring.cloud.authentication.entity.AuthClient;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface AuthClientRepository extends CrudRepository<AuthClient, UUID>, JpaSpecificationExecutor<AuthClient> {

    AuthClient findByClientId(String clientId);

    boolean existsByClientId(String clientId);
}
