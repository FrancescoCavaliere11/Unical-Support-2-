package unical_support.unicalsupport2.data.dto.email;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidIdFormat;

@Data
public class UpdateSingleClassificationDto {
    @NotNull(message = "Category id is required")
    @ValidIdFormat
    private String categoryId;

    @NotNull
    @NotBlank
    @Size(min = 1, max = 500)
    private String text;
}
