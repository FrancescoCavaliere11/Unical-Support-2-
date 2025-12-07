package unical_support.unicalsupport2.data.dto.classifier;

import lombok.Data;

import java.util.List;

@Data
public class ClassificationResultDto {
    private List<SingleCategoryDto> categories;
    private String explanation;
    private int id; // id per mantenere l'ordine della lista

    @Override
    public String toString() {
        return "Categories=" + categories + ", explanation=" + explanation;
    }
}