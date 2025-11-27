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

    private final PromptStrategyFactory factory;

    @Getter @Setter
    private String classifyCurrentStrategy = null;

    @Getter @Setter
    private String judgeCurrentStrategy = null;

    @Getter @Setter
    private String responderCurrentStrategy = null;


    private String resolve(String module, String moduleCurrentStrategy) {
        return moduleCurrentStrategy;
    }

    public String buildClassifyPrompt(List<ClassificationEmailDto> emails) {
        var strategy = factory.getStrategy("classifier", resolve("classifier", classifyCurrentStrategy));
        return strategy.buildClassifyPrompt(emails);
    }

    public String buildJudgePrompt(List<ClassificationEmailDto> emails, List<ClassificationResultDto> results) {

        var strategy = factory.getStrategy("judger", resolve("judger", judgeCurrentStrategy));
        return strategy.buildJudgePrompt(emails, results);
    }

    public String buildResponderPrompt(List<ClassificationResultDto> emails) {

        var strategy = factory.getStrategy("responder", resolve("responder", responderCurrentStrategy));
        return strategy.buildResponderPrompt(emails);
    }
}
