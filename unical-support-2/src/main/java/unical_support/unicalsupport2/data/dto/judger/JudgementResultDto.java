package unical_support.unicalsupport2.data.dto.judger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgementResultDto {
    private Integer id;                           // indice dell’email giudicata
    private List<CategoryEvaluationDto> categoriesEvaluation;
    private Double overallConfidence;            // [0,1] fiducia complessiva del giudizio
    private String summary;                      // sintesi del giudizio complessivo
    
}
