package unical_support.unicalsupport2.data.dto.email;

import java.util.List;

import lombok.Data;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidIdFormat;

@Data
public class UpdateEmailCategoryDto {
    @ValidIdFormat
    private String id;
    
    private List<@ValidIdFormat String> categoryIds;
}
