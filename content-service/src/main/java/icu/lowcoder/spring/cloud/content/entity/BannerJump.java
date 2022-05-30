package icu.lowcoder.spring.cloud.content.entity;

import icu.lowcoder.spring.commons.jpa.auditing.AuditingEntity;
import icu.lowcoder.spring.cloud.content.enums.JumpType;
import icu.lowcoder.spring.cloud.content.helper.JsonbType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Map;
import java.util.UUID;

/**
 * @Author: yanhan
 * @Description:
 * @Date: create in 2021/3/1 10:37 上午
 */
@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@SQLDelete(sql = "update banner_jump set deleted = true where id = ? ")
@Where(clause = "deleted = false")
@TypeDef(name = "JsonbType", typeClass = JsonbType.class)
public class BannerJump extends AuditingEntity {
    @OneToOne
    private Banner banner;

    /**跳转方式*/
    @Enumerated(EnumType.STRING)
    private JumpType type;

    /**跳转参数*/
    @Column(columnDefinition = "jsonb")
    @Type(type = "JsonbType")
    private Map<String,Object> params;


    private Boolean deleted = false;
    @Id
    @GeneratedValue(generator = "bannerJumpIdGenerator")
    private UUID id;
}
