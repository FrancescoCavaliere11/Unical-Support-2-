package unical_support.unicalsupport2.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class EmailMessage {
    private String inReplyToHeader;
    private String referencesHeader;
    private List<String> to;
    private String subject;
    private String body;
}
