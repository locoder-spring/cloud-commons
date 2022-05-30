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
@SQLDelete(sql = "update authority set deleted = true where id = ? ")
@Where(clause = "deleted = false")
public class Authority extends AuditingEntity {

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @ColumnDefault("false")
    private Boolean builtIn = false; // 内置的，无法修改删除

    @ManyToMany
    @JoinTable(
            name = "role_authority",
            joinColumns = {
                    @JoinColumn(name = "authority_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "role_id")
            }
    )
    @Where(clause = "deleted = false")
    private List<Role> roles = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "employee_authority",
            joinColumns = {
                    @JoinColumn(name = "authority_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "employee_id")
            }
    )
    @Where(clause = "deleted = false")
    private List<Employee> employees = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "position_authority",
            joinColumns = {
                    @JoinColumn(name = "authority_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "position_id")
            }
    )
    @Where(clause = "deleted = false")
    private List<Position> positions = new ArrayList<>();

    @ColumnDefault("false")
    private Boolean deleted = false;
}
