package unical_support.unicalsupport2.data.entities;

import java.util.List;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import lombok.Data;

@Data
@Entity
@Table(name = "emails")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "email_type", discriminatorType = DiscriminatorType.STRING)
public class Email {
    @Id
    @UuidGenerator
    private String id;

    @Column(name = "in_reply_to_header")
    private String inReplyToHeader;

    @Column(name = "references_header")
    private String referencesHeader;

    @Column(name = "recipients", nullable = false)
    private List<String> to;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;
}
