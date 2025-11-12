package unical_support.unicalsupport2.prompting;

import unical_support.unicalsupport2.data.dto.ClassificationEmailDto;

import java.util.List;

public interface PromptStrategy {
    String buildClassifyPrompt(List<ClassificationEmailDto> classificationEmailDtos);
}
