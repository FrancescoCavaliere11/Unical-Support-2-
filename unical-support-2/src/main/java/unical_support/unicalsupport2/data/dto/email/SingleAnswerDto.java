package unical_support.unicalsupport2.data.dto.email;

import lombok.Data;
import unical_support.unicalsupport2.data.dto.template.TemplateDto;

@Data
public class SingleAnswerDto {
    private TemplateDto template;
    private String answer;
}
