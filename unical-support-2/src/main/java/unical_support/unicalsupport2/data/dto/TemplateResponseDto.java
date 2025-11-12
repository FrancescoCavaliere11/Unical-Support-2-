package unical_support.unicalsupport2.data.dto;

import lombok.Data;
import unical_support.unicalsupport2.data.entities.Template;

import java.util.List;

@Data
public class TemplateResponseDto {
    List<Template> templates;
    String category;
    String emailMessage;
    // todo confidence ?
}
