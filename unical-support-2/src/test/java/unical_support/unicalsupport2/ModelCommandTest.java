package unical_support.unicalsupport2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unical_support.unicalsupport2.commands.ModelCommand;
import unical_support.unicalsupport2.runtime.ActiveLlmRegistry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModelCommandTest {

    @Mock
    private ActiveLlmRegistry registry;


    @Test
    void select_invalidProvider_returnsError() {
        ModelCommand cmd = new ModelCommand(registry);
        String out = cmd.select("openai");

        assertTrue(out.toLowerCase().contains("valore non valido"));
        verify(registry, never()).set(anyString());
    }
}
