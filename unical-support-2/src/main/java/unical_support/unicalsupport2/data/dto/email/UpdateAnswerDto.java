package unical_support.unicalsupport2.data.dto.email;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidAnswerId;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidSingleAnswersLength;

import java.util.List;

@Data
@ValidSingleAnswersLength
public class UpdateAnswerDto {
    @ValidAnswerId
    @NotBlank
    private String id;

    @Valid
    @NotNull
    private List<UpdateSingleAnswerDto> singleAnswers;
}
