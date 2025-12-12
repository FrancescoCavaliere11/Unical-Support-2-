package unical_support.unicalsupport2.service.interfaces;

import unical_support.unicalsupport2.data.dto.classifier.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;

import java.util.List;

public interface PromptService {
    String buildClassifyPrompt(List<ClassificationEmailDto> emails);
    String buildJudgePrompt(List<ClassificationEmailDto> emails, List<ClassificationResultDto> results);
    String buildResponderPrompt(List<ClassificationResultDto> emails);
}
