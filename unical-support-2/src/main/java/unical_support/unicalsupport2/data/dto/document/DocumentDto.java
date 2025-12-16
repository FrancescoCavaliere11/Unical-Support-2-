package unical_support.unicalsupport2.data.dto.document;

import lombok.Data;

import java.util.Date;

@Data
public class DocumentDto {
    private String id;
    private String originalFilename;
    private String documentLink;
    private Date createInDate;
}
