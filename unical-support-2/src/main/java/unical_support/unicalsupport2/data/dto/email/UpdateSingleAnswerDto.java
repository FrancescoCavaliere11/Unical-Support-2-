package unical_support.unicalsupport2.data.dto.email;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidIdFormat;

@Data
public class UpdateSingleAnswerDto {
    @ValidIdFormat
    private String templateId;

    @NotBlank(message = "answer cannot be blank")
    @Size(max = 5000, message = "max lenght for answer cannot exceed 5000 characters")
    private String answer;
}
