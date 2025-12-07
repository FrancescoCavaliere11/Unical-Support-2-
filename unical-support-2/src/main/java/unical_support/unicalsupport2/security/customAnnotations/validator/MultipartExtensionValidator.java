package unical_support.unicalsupport2.security.customAnnotations.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidMultipartExtension;

import java.util.List;

public class MultipartExtensionValidator implements ConstraintValidator<ValidMultipartExtension, MultipartFile> {
    private final List<String> allowedMimeType = List.of(
            "application/pdf",
            "application/msword", // .doc
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "text/plain" // .txt
    );

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null) return true;
        try {
            if (!allowedMimeType.contains(file.getContentType())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Invalid image file extension. Allowed extensions are: jpg, jpeg, png")
                        .addConstraintViolation();
                return false;
            }
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
}
