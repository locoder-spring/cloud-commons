package icu.lowcoder.spring.cloud.config.db.entity;

import icu.lowcoder.spring.commons.jpa.auditing.AuditingEntity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;

@Getter
@Setter
@Entity
@SQLDelete(sql = "update properties set deleted = true where id = ? ")
@Where(clause = "deleted = false")
public class Properties extends AuditingEntity {

    @Column(nullable = false)
    private String application;

    @Column(nullable = false)
    private String profile;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String key;

    private String value;

    @ColumnDefault("true")
    private Boolean deleted = false;
}
