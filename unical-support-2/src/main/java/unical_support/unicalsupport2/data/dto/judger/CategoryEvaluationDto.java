package unical_support.unicalsupport2.data.dto.judger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEvaluationDto {
    private String category;     // nome categoria proposta
    private Double confidence;   // [0,1] affidabilità del giudizio sulla singola categoria
    private String explanation;  // motivazione sintetica
    private String verdict;
}
