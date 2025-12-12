package unical_support.unicalsupport2.data.dto.email;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

// todo controlli sulla size della lista con le answer effettiva
@Data
public class UpdateAnswerDto {
    // todo controlli sull'id
    private String id;

    @Valid
    private List<UpdateSingleAnswerDto> singleAnswers;
}
