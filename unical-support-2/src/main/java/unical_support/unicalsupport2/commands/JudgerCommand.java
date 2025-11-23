package unical_support.unicalsupport2.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import unical_support.unicalsupport2.runtime.ActiveJudgerLlmRegistry;
import unical_support.unicalsupport2.runtime.ActiveJudgerPromptStrategyRegistry;

@Command(command = "judger", alias = "j", description = "Gestione del Judger (LLM e Prompt Strategy).")
@RequiredArgsConstructor
public class JudgerCommand {

    private final ActiveJudgerLlmRegistry llmRegistry;
    private final ActiveJudgerPromptStrategyRegistry promptRegistry;


    @Command(command = "model-list",alias = "ml", description = "Elenca i provider LLM disponibili per il Judger.")
    public String listModels() {
        return "LLM disponibili per il Judger: gemini, groq";
    }

    @Command(command = "model-current",alias = "mc", description = "Mostra il modello LLM attualmente usato dal Judger.")
    public String currentModel() {
        String cur = llmRegistry.get();
        return "Judger LLM attivo: " +
                (cur == null ? "(fallback dal classifier / application.properties)" : cur);
    }

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
        return "Judger LLM impostato su: " + p;
    }



    @Command(command = "prompt-list",alias = "pl", description = "Elenca le strategie di prompting disponibili per il Judger.")
    public String listPromptStrategies() {
        return "Strategie di prompting disponibili per il Judger: few, cot";
    }

    @Command(command = "prompt-current", alias = "pc", description = "Mostra la strategia di prompting attiva.")
    public String currentPromptStrategy() {
        String cur = promptRegistry.get();
        return "Strategia di prompting del Judger: " +
                (cur == null ? "(default: few)" : cur);
    }

    @Command(command = "prompt-set",alias = "ps", description = "Imposta la strategia di prompting (few | cot).")
    public String setPromptStrategy(
            @Option(longNames = "strategy", shortNames = 's') String strategy
    ) {
        if (strategy == null)
            return "Errore: specifica --strategy few|cot";

        String s = strategy.trim().toLowerCase();
        if (!s.equals("few") && !s.equals("cot"))
            return "Valore non valido. Usa: few | cot";

        promptRegistry.set(s);
        return "Strategia di prompting del Judger impostata su: " + s;
    }
}
