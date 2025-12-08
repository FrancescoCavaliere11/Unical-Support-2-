package unical_support.unicalsupport2.data.dto.email;

import lombok.Data;

import java.util.List;

@Data
public class ClassifyDto {
    private String explanation;
    private List<SingleClassificationDto> singleClassifications;
}
