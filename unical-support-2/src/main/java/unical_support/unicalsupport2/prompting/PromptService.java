package unical_support.unicalsupport2.prompting;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromptService {
    private final PromptStrategyFactory promptStrategyFactory;

    @Getter
    @Setter
    private String currentStrategy = "fewShot";


    public String buildClassifyPrompt(List<ClassificationEmailDto> classificationEmailDtos) {
        PromptStrategy strategy = promptStrategyFactory.getStrategy(currentStrategy);
        return strategy.buildClassifyPrompt(classificationEmailDtos);
    }
    public String buildJudgePrompt(List<ClassificationEmailDto> emails,
                                   List<ClassificationResultDto> results) {
        PromptStrategy strategy = promptStrategyFactory.getStrategy(currentStrategy);
        return strategy.buildJudgePrompt(emails, results);
    }

    public String buildResponderPrompt(List<unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto> emails) {
        PromptStrategy strategy = promptStrategyFactory.getStrategy(currentStrategy);
        return strategy.buildResponderPrompt(emails);
    }
}