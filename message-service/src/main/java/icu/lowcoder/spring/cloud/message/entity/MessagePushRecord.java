package icu.lowcoder.spring.cloud.message.entity;

import icu.lowcoder.spring.commons.jpa.auditing.AuditingEntity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Setter
@SQLDelete(sql = "update message_push_record set deleted = true where id = ? ")
@Where(clause = "deleted = false")
public class MessagePushRecord extends AuditingEntity {

    @ColumnDefault("false")
    private Boolean success = false;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Column(columnDefinition = "TEXT")
    private String target;

    @Column(columnDefinition = "TEXT")
    private String remark;

    private String channel;

    @ManyToOne
    private Message message;

    @ColumnDefault("false")
    private Boolean deleted = false;

}
