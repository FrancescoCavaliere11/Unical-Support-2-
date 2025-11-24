package unical_support.unicalsupport2.data.dto.email;

import lombok.Data;

import java.util.List;

@Data
public abstract class EmailDto {
    private String id;
    private List<String> from;
    private String subject;
    private String content;
}
