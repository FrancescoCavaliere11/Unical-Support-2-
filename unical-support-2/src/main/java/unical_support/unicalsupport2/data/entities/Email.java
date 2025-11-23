package unical_support.unicalsupport2.data.entities;

import java.util.List;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "emails")
@Data
public class Email {
    
    @Id
    @UuidGenerator
    @Column(name = "email_id")
    private String id;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "email_destinatari", joinColumns = @JoinColumn(name="email_id"))
    @Column(name = "destinatari")
    private List<String> to;

    @Column(name = "classificata", nullable = false)
    private boolean classified;

    @Column(name = "oggetto", nullable = false)
    private String subject;

    @Column(name = "testo")
    private String body;
}
