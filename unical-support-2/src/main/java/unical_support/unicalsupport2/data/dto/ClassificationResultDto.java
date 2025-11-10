package unical_support.unicalsupport2.data.dto;

import lombok.Data;
import java.util.List;

@Data
public class ClassificationResultDto {
    private final List<SingleCategoryDto> categories;
    private final String explanation;

    @Override
    public String toString() {
        return "Categories=" + categories + ", explanation=" + explanation;
    }
}