package icu.lowcoder.spring.cloud.content.entity;

import icu.lowcoder.spring.commons.jpa.auditing.AuditingEntity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
public class BannerTag extends AuditingEntity {

    private String name;

    @ManyToMany
    @JoinTable(
            name = "banner_tags",
            joinColumns = {
                    @JoinColumn(name = "tag_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "banner_id")
            }
    )
    @Where(clause = "deleted = false")
    private List<Banner> banners = new ArrayList<>();
}
