package unical_support.unicalsupport2.data.embeddables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class TemplateParameter {
    @Column(name = "param_name", nullable = false)
    private String name;

    @Column(name = "param_required", nullable = false)
    private boolean required = true;

    //TODO: potrebbe servire ma attualmente preferisco non metterla, vediamo in futuro
//    @Column(name = "param_description")
//    private String description;

    @Override
    public String toString() {
        return "{name='" + name + "', required=" + required + "}";
    }
}