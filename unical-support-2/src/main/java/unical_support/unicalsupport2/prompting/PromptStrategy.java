package unical_support.unicalsupport2.prompting;

import unical_support.unicalsupport2.data.dto.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.ClassificationResultDto;

import java.util.List;

public interface PromptStrategy {
    String buildClassifyPrompt(List<ClassificationEmailDto> classificationEmailDtos);
    String buildJudgePrompt(List<ClassificationEmailDto> classificationEmailDtos, List<ClassificationResultDto> results);
}
