package unical_support.unicalsupport2.data.dto.classifier;

import lombok.Data;

import java.util.List;

@Data
public class ClassificationResultDto {
    private final List<SingleCategoryDto> categories;
    private final String explanation;
    private final int id; // id per mantenere l'ordine della lista

    @Override
    public String toString() {
        return "Categories=" + categories + ", explanation=" + explanation;
    }
}