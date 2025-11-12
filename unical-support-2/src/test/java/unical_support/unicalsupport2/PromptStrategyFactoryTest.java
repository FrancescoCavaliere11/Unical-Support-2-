package unical_support.unicalsupport2;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import unical_support.unicalsupport2.prompting.PromptStrategy;
import unical_support.unicalsupport2.prompting.PromptStrategyFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PromptStrategyFactoryTest {
    @Test
    void getStrategy_returnsCorrectImplementation() {
        PromptStrategy fewShot = Mockito.mock(PromptStrategy.class);
        PromptStrategy zeroShot = Mockito.mock(PromptStrategy.class);

        PromptStrategyFactory factory = new PromptStrategyFactory(Map.of(
                "fewShot", fewShot,
                "zeroShotCoT", zeroShot
        ));

        assertThat(factory.getStrategy("fewShot")).isEqualTo(fewShot);
        assertThat(factory.getStrategy("zeroShotCoT")).isEqualTo(zeroShot);
        assertThat(factory.getStrategy("unknown")).isEqualTo(fewShot); // fallback
    }
}
