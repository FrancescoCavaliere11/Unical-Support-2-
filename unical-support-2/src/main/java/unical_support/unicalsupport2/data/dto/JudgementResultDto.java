package unical_support.unicalsupport2.data.dto;

import lombok.Data;
import java.util.List;

@Data
public class JudgementResultDto {


    @Data
    public static class CategoryEvaluation {
        private String category;       // Nome della categoria
        private Double confidence;     // Punteggio di affidabilità (0-1)
        private String explanation;    // Spiegazione sintetica e formale
    }

    private List<CategoryEvaluation> categoriesEvaluation;

    private Double overallConfidence;

    private String summary;
}
