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
@SQLDelete(sql = "update department set deleted = true where id = ? ")
@Where(clause = "deleted = false")
public class Department extends AuditingEntity {
    private String name;

    @Where(clause = "deleted = false")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent", orphanRemoval = true)
    private List<Department> children;

    @ManyToOne(fetch = FetchType.LAZY)
    private Department parent;

    @ManyToMany
    @JoinTable(
            name = "department_employee",
            joinColumns = {
                    @JoinColumn(name = "department_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "employee_id")
            }
    )
    @Where(clause = "deleted = false")
    private List<Employee> employees = new ArrayList<>();

    @ColumnDefault("false")
    private Boolean deleted = false;
}
