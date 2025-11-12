package unical_support.unicalsupport2.service.interfaces;

import unical_support.unicalsupport2.data.dto.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.JudgementResultDto;
import java.util.List;

public interface JudgePromptService {
    List<JudgementResultDto> buildJudgePrompt(List<ClassificationEmailDto> emails, List<ClassificationResultDto> results);
}
