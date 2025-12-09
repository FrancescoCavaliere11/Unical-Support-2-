package unical_support.unicalsupport2.data.dto.template;

import lombok.Data;
import lombok.EqualsAndHashCode;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidIdFormat;

@Data
@EqualsAndHashCode(callSuper = true)
public class TemplateUpdateDto extends TemplateDto {
    @ValidIdFormat
    private String id;
}
