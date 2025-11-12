package unical_support.unicalsupport2;

import org.junit.jupiter.api.Test;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationEmailDto;
import unical_support.unicalsupport2.prompting.PromptService;
import unical_support.unicalsupport2.prompting.PromptStrategy;
import unical_support.unicalsupport2.prompting.PromptStrategyFactory;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PromptServiceTest {

    @Test
    void canSwitchPromptStrategiesAtRuntime() {
        PromptStrategy few = mock(PromptStrategy.class);
        PromptStrategy cot = mock(PromptStrategy.class);
        when(few.buildClassifyPrompt(anyList())).thenReturn("FEW PROMPT");
        when(cot.buildClassifyPrompt(anyList())).thenReturn("COT PROMPT");

        PromptStrategyFactory factory = new PromptStrategyFactory(Map.of(
                "fewShot", few,
                "zeroShotCoT", cot
        ));

        PromptService service = new PromptService(factory);

        String prompt1 = service.buildClassifyPrompt(List.of(new ClassificationEmailDto()));
        assertThat(prompt1).isEqualTo("FEW PROMPT");

        service.setCurrentStrategy("zeroShotCoT");
        String prompt2 = service.buildClassifyPrompt(List.of(new ClassificationEmailDto()));
        assertThat(prompt2).isEqualTo("COT PROMPT");

        verify(few, times(1)).buildClassifyPrompt(anyList());
        verify(cot, times(1)).buildClassifyPrompt(anyList());
    }
}
