package unical_support.unicalsupport2.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import unical_support.unicalsupport2.runtime.ActiveLlmRegistry;

@Command(command = "llm", alias = "l", description = "Gestione provider LLM (gemini/groq)")
@RequiredArgsConstructor
public class ModelCommand {

    private final ActiveLlmRegistry registry;

    @Command(command = "list", alias = "ls", description = "Elenca i provider disponibili")
    public String list() {
        return "Provider disponibili: gemini, groq";
    }

    @Command(command = "current", alias = "c", description = "Mostra il provider attivo")
    public String current() {
        String cur = registry.get();
        return "Provider attivo: " + (cur == null || cur.isBlank() ? "(da properties)" : cur);
    }

    @Command(command = "select", alias = "s", description = "Seleziona il provider (gemini|groq)")
    public String select(
            @Option(longNames = "provider", shortNames = 'p', description = "gemini|groq") String provider
    ) {
        if (provider == null) return "Errore: specifica --provider gemini|groq";
        String p = provider.toLowerCase().trim();
        if (!p.equals("gemini") && !p.equals("groq")) return "Valore non valido. Usa: gemini | groq";
        registry.set(p);
        return "Provider impostato a runtime: " + p;
    }
}
