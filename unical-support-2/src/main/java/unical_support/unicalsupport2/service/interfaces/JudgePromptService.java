package unical_support.unicalsupport2.service.interfaces;

import unical_support.unicalsupport2.data.dto.classifier.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.JudgementResultDto;
import java.util.List;

public interface JudgePromptService {
    List<JudgementResultDto> buildJudgePrompt(List<ClassificationEmailDto> emails, List<ClassificationResultDto> results);
}
