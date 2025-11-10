package unical_support.unicalsupport2.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleCategoryDto {
    private String category;
    private double confidence;
    private String text;

    @Override
    public String toString() {
        return "name=" + category + ", confidence=" + confidence + ", text=" + text;
    }
}
