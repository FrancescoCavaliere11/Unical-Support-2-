package unical_support.unicalsupport2.prompting;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.data.dto.ClassificationEmailDto;

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
}