package unical_support.unicalsupport2.data.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;
import unical_support.unicalsupport2.data.embeddables.TemplateParameter;

import java.util.List;

@Entity
@Data
@Table(name = "templates")
public class Template {
    @Id
    @UuidGenerator
    private String id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "template_parameters",
            joinColumns = @JoinColumn(name = "template_id")
    )
    private List<TemplateParameter> parameters;

    @Override
    public String toString() {
        String cleanedContent = content != null ? content.replaceAll("[\\n\\r\\t]", " ") : "";
        return "{\n" +
                "\tname= '" + name + "'\n" +
                "\tcontent= '" + cleanedContent + "'\n" +
                "\tdescription= '" + description + "'\n" +
                "\tparameters= " + parameters.toString() + "\n" +
                "}\n\n";
    }
}
