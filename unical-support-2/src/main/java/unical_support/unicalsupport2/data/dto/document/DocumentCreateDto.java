package unical_support.unicalsupport2.data.dto.document;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidCategoryId;

@Data
public class DocumentCreateDto {
    @ValidCategoryId
    @NotBlank
    private String categoryId;

    @URL(protocol = "https", message = "invalid document url")
    private String documentLink;
}
