package unical_support.unicalsupport2.data.dto.email;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmailToClassifyDto extends EmailDto {
    private String explanation;
    private List<SingleClassificationDto> singleClassifications;
}
