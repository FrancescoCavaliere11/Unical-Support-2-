package unical_support.unicalsupport2.data.dto.template;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidParameterName;

@Data
public class ParameterDto {
    @ValidParameterName(message = "Parameter name must be in snake_case (e.g., 'user_question')")
    @Size(min = 1, max = 50, message = "Template name must be between 1 and 50 characters")
    private String name;

    @NotNull(message = "The 'required' flag is mandatory")
    private Boolean required;
}
