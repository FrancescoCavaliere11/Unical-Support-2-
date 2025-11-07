package unical_support.unicalsupport2.data.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ClassificationResultDto {
    private final Map<String, Double> categories;
    private final String explanation;

    @Override
    public String toString() {
        return "Categories=" + categories + ", explanation=" + explanation;
    }
}