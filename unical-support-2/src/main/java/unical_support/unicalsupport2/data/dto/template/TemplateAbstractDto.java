package unical_support.unicalsupport2.data.dto.template;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidIdFormat;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidParameters;

import java.util.List;

@Data
@ValidParameters
public abstract class TemplateAbstractDto {
    @NotBlank(message = "Template name cannot be empty or whitespace")
    @Size(min = 1, max = 50, message = "Template name must be between 1 and 50 characters")
    private String name;

    @ValidIdFormat
    @NotNull(message = "Category id is required")
    private String categoryId;

    @NotBlank(message = "Template content cannot be empty")
    @Size(min = 1, max = 5000, message = "Template content must be between 1 and 5000 characters")
    private String content;

    @NotBlank(message = "Template description cannot be empty")
    @Size(min = 1, max = 300, message = "Template content must be between 1 and 300 characters")
    private String description;

    @Valid
    private List<ParameterDto> parameters;
}
