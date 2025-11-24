package unical_support.unicalsupport2.data.embeddables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class SingleClassification {
    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "confidence", nullable = false)
    private double confidence;

    @Column(name = "text", nullable = false)
    private String text;
}
