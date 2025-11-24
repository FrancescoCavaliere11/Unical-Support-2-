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

    // Questo è l'id che otteniamo dagli header
    // dell'oggetto Message che otteniamo dal Receiver.
    // Potremmo ragionare se usarlo anche come ID dell'entità o meno
    @Column(name = "emailId", nullable = false)
    private String emailId;

    @Column(name = "to", nullable = false)
    private List<String> to;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "body", nullable = false)
    private String body;
}
