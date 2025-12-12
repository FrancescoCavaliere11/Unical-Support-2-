package unical_support.unicalsupport2.data.embeddables;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import unical_support.unicalsupport2.data.converters.MapToJsonConverter;
import unical_support.unicalsupport2.data.entities.Category;
import unical_support.unicalsupport2.data.entities.Template;

import java.util.Map;

@Data
@Embeddable
public class SingleAnswer {
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id")
    private Template template;

    @Column(name = "reason", nullable = false)
    private double reason;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Convert(converter = MapToJsonConverter.class)
    @Column(name = "parameters", columnDefinition = "TEXT")
    private Map<String, String> parameter;
}
