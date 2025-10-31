package unical_support.unicalsupport2.service.interfaces;

import unical_support.unicalsupport2.EmailClassifier.Model.EmailData;

import java.util.List;

public interface PromptService {
    String buildSystemMessage();

    String buildUserMessage(EmailData email);

    String buildSystemMessageBatch();

    String buildUserMessageBatch(List<EmailData> emails);
}
