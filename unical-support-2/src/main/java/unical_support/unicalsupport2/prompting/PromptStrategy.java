package unical_support.unicalsupport2.prompting;

import unical_support.unicalsupport2.data.dto.classifier.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;

import java.util.List;

public interface PromptStrategy {
    String buildClassifyPrompt(List<ClassificationEmailDto> classificationEmailDtos);
    String buildResponderPrompt(List<ClassificationResultDto> emails);
    String buildJudgePrompt(List<ClassificationEmailDto> classificationEmailDtos, List<ClassificationResultDto> results);
}
