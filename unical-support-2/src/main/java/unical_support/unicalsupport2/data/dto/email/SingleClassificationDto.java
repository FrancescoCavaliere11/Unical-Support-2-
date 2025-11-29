package unical_support.unicalsupport2.data.dto.email;

import lombok.Data;
import unical_support.unicalsupport2.data.dto.CategoryDto;

@Data
public class SingleClassificationDto {
    private CategoryDto category;
    private double confidence;
    private String text;
}
