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
import java.util.UUID;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@SQLDelete(sql = "update employee set deleted = true where id = ? ")
@Where(clause = "deleted = false")
public class Employee extends AuditingEntity {

    @Column(nullable = false)
    private String name;

    private String no;

    @Column(nullable = false)
    private String phone;

    private String email;

    private Boolean enabled = true;

    private UUID accountId;

    // 部门关系
    @ManyToMany
    @JoinTable(
            name = "department_employee",
            joinColumns = {
                    @JoinColumn(name = "employee_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "department_id")
            }
    )
    @Where(clause = "deleted = false")
    private List<Department> departments = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "employee_role",
            joinColumns = {
                    @JoinColumn(name = "employee_id")
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
                    @JoinColumn(name = "employee_id")
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
