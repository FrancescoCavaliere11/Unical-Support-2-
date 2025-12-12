package unical_support.unicalsupport2.data.dto.email;

import lombok.Data;

import java.util.List;

@Data
public class ClassifyDto {
    private String id;
    private String explanation;
    private Boolean classified;
    private List<SingleClassificationDto> singleClassifications;
}
