package unical_support.unicalsupport2.service.interfaces;

import unical_support.unicalsupport2.data.dto.ClassificationEmailDto;

import java.util.List;

public interface PromptService {
    String buildSystemMessage();

    String buildUserMessage(ClassificationEmailDto classificationEmailDto);

    String buildSystemMessageBatch();

    String buildUserMessageBatch(List<ClassificationEmailDto> classificationEmailDtos);
}
