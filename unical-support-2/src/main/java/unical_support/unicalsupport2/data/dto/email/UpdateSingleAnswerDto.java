package unical_support.unicalsupport2.data.dto.email;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSingleAnswerDto {
    @NotBlank(message = "answer cannot be blank")
    @Size(max = 500, message = "max lenght for answer cannot exceed 500 characters")
    private String answer;
}
