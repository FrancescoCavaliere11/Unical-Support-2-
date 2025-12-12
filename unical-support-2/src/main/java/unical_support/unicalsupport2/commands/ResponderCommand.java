package unical_support.unicalsupport2.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import unical_support.unicalsupport2.runtime.ActiveJudgerLlmRegistry;

@Command(command = "responder", alias = "r", description = "Gestione del Responder (LLM e Prompt Strategy).")
@RequiredArgsConstructor
public class ResponderCommand {
    private final ActiveJudgerLlmRegistry llmRegistry;

    @Command(command = "model-set",alias = "ms", description = "Imposta il provider LLM per il Judger (gemini | groq).")
    public String setModel(
            @Option(longNames = "provider", shortNames = 'p') String provider
    ) {
        if (provider == null)
            return "Errore: specifica --provider gemini|groq";

        String p = provider.trim().toLowerCase();
        if (!p.equals("gemini") && !p.equals("groq"))
            return "Valore non valido. Usa: gemini | groq";

        llmRegistry.set(p);
        return "Responder LLM impostato su: " + p;
    }
}
