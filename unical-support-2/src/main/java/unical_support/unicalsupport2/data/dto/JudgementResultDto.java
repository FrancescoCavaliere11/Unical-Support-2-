package unical_support.unicalsupport2.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgementResultDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryEvaluation {
        private String category;     // nome categoria proposta
        private Double confidence;   // [0,1] affidabilità del giudizio sulla singola categoria
        private String explanation;  // motivazione sintetica
        private String verdict;
    }

    private Integer id;                           // indice dell’email giudicata
    private List<CategoryEvaluation> categoriesEvaluation;
    private Double overallConfidence;            // [0,1] fiducia complessiva del giudizio
    private String summary;                      // sintesi del giudizio complessivo
    
}
