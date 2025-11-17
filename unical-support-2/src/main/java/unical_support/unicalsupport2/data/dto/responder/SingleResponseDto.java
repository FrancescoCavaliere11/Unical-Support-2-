package unical_support.unicalsupport2.data.dto.responder;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Map;

@Data
@AllArgsConstructor
public class SingleResponseDto {
    private String category;
    private String template;
    private String content;
    private Map<String, String> parameter;
    private String reason;
}
