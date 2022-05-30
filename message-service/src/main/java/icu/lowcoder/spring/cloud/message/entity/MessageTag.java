package icu.lowcoder.spring.cloud.message.entity;

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
public class MessageTag extends AuditingEntity {

    private String name;

    @ManyToMany
    @JoinTable(
            name = "message_tags",
            joinColumns = {
                    @JoinColumn(name = "tag_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "message_id")
            }
    )
    @Where(clause = "deleted = false")
    private List<Message> messages = new ArrayList<>();
}
