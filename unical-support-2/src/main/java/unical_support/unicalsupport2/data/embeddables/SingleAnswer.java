package unical_support.unicalsupport2.data.embeddables;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import unical_support.unicalsupport2.data.entities.Template;

import java.util.Map;

@Data
@Embeddable
public class SingleAnswer {
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private Template template;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;
}
