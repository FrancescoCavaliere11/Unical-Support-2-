package unical_support.unicalsupport2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import unical_support.unicalsupport2.prompting.PromptStrategy;
import unical_support.unicalsupport2.prompting.PromptStrategyFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PromptStrategyFactoryTest {

    private PromptStrategy fewShot;
    private PromptStrategy zeroShot;
    private PromptStrategyFactory factory;


    @BeforeEach
    void setup() throws Exception {
        fewShot = Mockito.mock(PromptStrategy.class);
        zeroShot = Mockito.mock(PromptStrategy.class);

        factory = new PromptStrategyFactory(Map.of(
                "fewShot", fewShot,
                "zeroShotCoT", zeroShot
        ));


        Map<String, Object> mockConfig = new HashMap<>();
        mockConfig.put("modules", Map.of(
                "classifier", Map.of("default", "fewShot"),
                "responder", Map.of("default", "zeroShotCoT")
        ));

        Field configField = PromptStrategyFactory.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(factory, mockConfig);
    }

    @Test
    void getStrategy_returnsCorrectImplementation() {
        Map<String, Object> mockConfig = Map.of(
                "prompt", Map.of(
                        "modules", Map.of(
                                "classifier", Map.of("default", "fewShot"),
                                "responder", Map.of("default", "zeroShotCoT")
                        )
                )
        );

        ReflectionTestUtils.setField(factory, "config", mockConfig);

        assertThat(factory.getStrategy("classifier", "fewShot"))
                .isEqualTo(fewShot);

        assertThat(factory.getStrategy("classifier", "zeroShotCoT"))
                .isEqualTo(zeroShot);

        assertThat(factory.getStrategy("classifier", null))
                .isEqualTo(fewShot);

        assertThat(factory.getStrategy("responder", null))
                .isEqualTo(zeroShot);

        assertThat(factory.getStrategy("unknown"))
                .isEqualTo(fewShot);
    }
}
