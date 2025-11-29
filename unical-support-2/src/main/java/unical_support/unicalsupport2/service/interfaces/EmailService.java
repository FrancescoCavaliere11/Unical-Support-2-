package unical_support.unicalsupport2.service.interfaces;

import java.util.List;

import unical_support.unicalsupport2.data.EmailMessage;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.email.EmailToClassifyDto;
import unical_support.unicalsupport2.data.dto.email.UpdateEmailCategoryDto;

public interface EmailService {
    List<EmailToClassifyDto> getStoredEmail(Boolean isClassified);

    void updateEmailCategory(UpdateEmailCategoryDto updateEmailCategoryDto);
    
    void saveEmailWithLowConfidence(EmailMessage emailToSave, ClassificationResultDto classificationResultDto);
}
