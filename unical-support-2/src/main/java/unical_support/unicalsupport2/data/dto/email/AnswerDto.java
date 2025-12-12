package unical_support.unicalsupport2.data.dto.email;

import lombok.Data;

import java.util.List;

@Data
public class AnswerDto {
    private String id;
    private Boolean answered;
    private List<SingleAnswerDto> singleAnswers;
}
