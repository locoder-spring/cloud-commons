package icu.lowcoder.spring.cloud.authentication.entity;

import icu.lowcoder.spring.commons.jpa.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@SQLDelete(sql = "update auth_client set deleted = true where id = ? ")
@Where(clause = "deleted = false")
public class AuthClient extends CommonEntity {

    @Column(nullable = false)
    private String clientId;
    @Column(nullable = false)
    private String clientSecret;
    @Column(nullable = false)
    private String clientName;

    private String grantTypes;

    private String scopes;

    private String authorities;

    @ColumnDefault("false")
    private Boolean deleted = false;
    @Id
    @GeneratedValue(generator = "idGenerator")
    private UUID id;
}
