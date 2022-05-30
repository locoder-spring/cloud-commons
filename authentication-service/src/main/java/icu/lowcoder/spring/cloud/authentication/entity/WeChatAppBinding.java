package icu.lowcoder.spring.cloud.authentication.entity;

import icu.lowcoder.spring.commons.jpa.auditing.AuditingEntity;
import icu.lowcoder.spring.cloud.authentication.dict.WeChatAppType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.UUID;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@SQLDelete(sql = "update we_chat_app_binding set deleted = true where id = ? ")
@Where(clause = "deleted = false")
public class WeChatAppBinding extends AuditingEntity {

    @ManyToOne
    private Account account;

    @Column(nullable = false)
    private String openId;

    @Column(nullable = false)
    private String appId;

    private String unionId;

    @Enumerated(EnumType.STRING)
    private WeChatAppType appType;

    private Boolean deleted = false;
    @Id
    @GeneratedValue(generator = "idGenerator")
    private UUID id;
}
