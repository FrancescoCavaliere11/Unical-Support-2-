package unical_support.unicalsupport2.service.interfaces;

import java.util.List;

import unical_support.unicalsupport2.data.EmailMessage;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.email.EmailDto;
import unical_support.unicalsupport2.data.dto.email.UpdateEmailCategoryDto;
import unical_support.unicalsupport2.data.dto.responder.ResponderResultDto;
import unical_support.unicalsupport2.data.entities.Email;

public interface EmailService {
    List<EmailDto> getStoredEmail();

    void updateEmailCategory(UpdateEmailCategoryDto updateEmailCategoryDto);
    
    Email saveEmail(EmailMessage emailToSave, ClassificationResultDto classificationResultDto);

    void saveAnswers(Email email, ResponderResultDto responderResultDto);
}
