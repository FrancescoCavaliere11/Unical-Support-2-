package unical_support.unicalsupport2.service.interfaces;

import java.util.List;

import unical_support.unicalsupport2.data.EmailMessage;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.email.EmailDto;
import unical_support.unicalsupport2.data.dto.email.UpdateAnswerDto;
import unical_support.unicalsupport2.data.dto.email.UpdateEmailCategoryDto;

public interface EmailService {
    List<EmailDto> getStoredEmail();

    void updateEmailCategory(UpdateEmailCategoryDto updateEmailCategoryDto);
    
    void saveEmail(EmailMessage emailToSave, ClassificationResultDto classificationResultDto);

    EmailDto updateAndSendEmail(UpdateAnswerDto updateAnswerDto);
}
