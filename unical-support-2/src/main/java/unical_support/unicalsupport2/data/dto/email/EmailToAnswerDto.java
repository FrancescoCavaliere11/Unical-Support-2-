package unical_support.unicalsupport2.data.dto.email;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmailToAnswerDto extends EmailDto {
    List<SingleAnswerDto> singleAnswers;
}
