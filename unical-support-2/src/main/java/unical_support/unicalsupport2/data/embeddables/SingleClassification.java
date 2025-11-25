package unical_support.unicalsupport2.data.embeddables;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import unical_support.unicalsupport2.data.entities.Category;

@Data
@Embeddable
public class SingleClassification {
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "confidence", nullable = false)
    private double confidence;

    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;
}
