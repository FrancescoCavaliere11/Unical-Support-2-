package unical_support.unicalsupport2.data.dto.email;

import lombok.Data;

import java.util.List;

@Data
public class AnswerDto {
    String id;
    List<SingleAnswerDto> singleAnswers;
}
