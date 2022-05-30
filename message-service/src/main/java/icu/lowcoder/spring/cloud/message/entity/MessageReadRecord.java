package icu.lowcoder.spring.cloud.message.entity;

import icu.lowcoder.spring.commons.jpa.auditing.AuditingEntity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
@Getter
@Setter
@SQLDelete(sql = "update message_push_record set deleted = true where id = ? ")
@Where(clause = "deleted = false")
public class MessageReadRecord extends AuditingEntity {

    private Date readTime; // 读消息时间

    @ManyToOne
    private Message message;

    @ColumnDefault("false")
    private Boolean deleted = false;

}
