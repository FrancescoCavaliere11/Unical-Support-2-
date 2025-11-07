package unical_support.unicalsupport2.data.dto;

import lombok.Data;

@Data
public class ClassificationResultDto {
    private final String category;
    private final double confidence;
    private final String explanation;

    @Override
    public String toString() {
        return "\nCategory= " + category + "\nConfidence= " + confidence + "\nExplanation= " + explanation + "\n\n";
    }
}