package unical_support.unicalsupport2.data.dto.email;

import lombok.Data;

@Data
public class SingleClassificationDto {
    private String category;
    private double confidence;
    private String text;
}
