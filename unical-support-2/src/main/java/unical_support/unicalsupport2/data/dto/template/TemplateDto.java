package unical_support.unicalsupport2.data.dto.template;

import lombok.Data;
import unical_support.unicalsupport2.data.dto.CategoryDto;

import java.util.List;

@Data
public class TemplateDto {
    private String id;
    private String name;
    private CategoryDto category;
    private String content;
    private List<ParameterDto> parameters;
}
