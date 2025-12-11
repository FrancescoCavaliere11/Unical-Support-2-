package unical_support.unicalsupport2.data.dto.email;

import lombok.Data;

import java.util.List;

@Data
public class EmailDto {
    private String id;
    private List<String> to;
    private String subject;
    private String body;
    private ClassifyDto classify;
    private AnswerDto answer;
}
