package unical_support.unicalsupport2.data.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import unical_support.unicalsupport2.data.embeddables.SingleClassification;

import java.util.List;

@Data
@Entity
@Table(name = "classifications")
public class Classifications {
    @Id
    @UuidGenerator
    private String id;

    @Column(name = "is_classified", nullable = false)
    private boolean isClassified;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "single_classification",
            joinColumns = @JoinColumn(name = "email_id")
    )
    private List<SingleClassification> singleClassifications;

    @OneToOne(mappedBy = "classifications", cascade = CascadeType.ALL, optional = false)
    private Email email;
}
