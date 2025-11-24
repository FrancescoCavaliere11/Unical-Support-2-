package unical_support.unicalsupport2.data.dto.email;

import lombok.Data;
import lombok.EqualsAndHashCode;
import unical_support.unicalsupport2.data.dto.judger.CategoryEvaluationDto;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmailToClassifyDto extends EmailDto {
    List<CategoryEvaluationDto> categoriesEvaluationDto;
}
