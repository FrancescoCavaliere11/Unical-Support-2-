package unical_support.unicalsupport2.data.dto.responder;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SingleEmailRequestDto {
    @NotBlank
    private String subject;

    @NotBlank
    private String body;
}