package unical_support.unicalsupport2.data.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import unical_support.unicalsupport2.data.embeddables.SingleClassification;

import java.util.List;

@Data
@Entity
@DiscriminatorValue("to_classify")
@EqualsAndHashCode(callSuper = true)
public class EmailToClassify extends Email {

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
}
