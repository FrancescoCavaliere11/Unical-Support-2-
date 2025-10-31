package unical_support.unicalsupport2.service.interfaces;

import unical_support.unicalsupport2.EmailClassifier.Model.ClassificationResult;
import unical_support.unicalsupport2.EmailClassifier.Model.EmailData;

import java.util.List;

public interface EmailClassifier {

    ClassificationResult classifyEmail(EmailData emailData);

    List<ClassificationResult> classifyEmailBatch(List<EmailData> emails);
}
