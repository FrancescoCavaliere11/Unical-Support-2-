package unical_support.unicalsupport2.data.dto.responder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import unical_support.unicalsupport2.data.dto.classifier.SingleCategoryDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingleEmailResponseDto {

    private List<SingleCategoryDto> classification;

    private Double judgerConfidence;

    private String judgerVerdict;

    private List<SingleResponseDto> generatedResponses;
}