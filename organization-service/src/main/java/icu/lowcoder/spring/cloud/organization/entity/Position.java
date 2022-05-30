package icu.lowcoder.spring.cloud.organization.entity;

import icu.lowcoder.spring.commons.jpa.auditing.AuditingEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@SQLDelete(sql = "update position set deleted = true where id = ? ")
@Where(clause = "deleted = false")
public class Position extends AuditingEntity {

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @ColumnDefault("false")
    private Boolean builtIn = false; // 内置的，无法修改删除

    @ManyToMany
    @JoinTable(
            name = "position_authority",
            joinColumns = {
                    @JoinColumn(name = "position_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "authority_id")
            }
    )
    @Where(clause = "deleted = false")
    private List<Authority> authorities = new ArrayList<>();

    @ColumnDefault("false")
    private Boolean deleted = false;

}
