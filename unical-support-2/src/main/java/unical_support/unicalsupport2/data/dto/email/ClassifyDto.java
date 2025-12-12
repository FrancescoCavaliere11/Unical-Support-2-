package unical_support.unicalsupport2.data.dto.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ClassifyDto {
    String id;
    private String explanation;
    @JsonProperty("isClassified") private boolean isClassified;
    private List<SingleClassificationDto> singleClassifications;
}
