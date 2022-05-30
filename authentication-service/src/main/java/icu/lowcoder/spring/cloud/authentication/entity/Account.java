package icu.lowcoder.spring.cloud.authentication.entity;

import icu.lowcoder.spring.commons.jpa.auditing.AuditingEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@SQLDelete(sql = "update account set deleted = true where id = ? ")
@Where(clause = "deleted = false")
public class Account extends AuditingEntity {

    private String name;

    private String phone;

    private String email;

    private String password;

    private String qq;

    private Date registerTime = new Date();

    private Boolean enabled = true;

    private String authorities; // 默认授权

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "account", orphanRemoval = true)
    @Where(clause = "deleted = false")
    private List<WeChatAppBinding> weChatAppBindings = new ArrayList<>();

    private Boolean deleted = false;
    @Id
    @GeneratedValue(generator = "idGenerator")
    private UUID id;
}
