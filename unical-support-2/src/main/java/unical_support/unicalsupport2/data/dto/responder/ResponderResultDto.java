package unical_support.unicalsupport2.data.dto.responder;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ResponderResultDto {
    private int emailId;
    private List<SingleResponseDto> responses;
}
