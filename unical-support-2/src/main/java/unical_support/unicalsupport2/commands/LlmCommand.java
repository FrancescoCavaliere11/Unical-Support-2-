package unical_support.unicalsupport2.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import unical_support.unicalsupport2.configurations.factory.LlmProperties;
import unical_support.unicalsupport2.configurations.factory.LlmStrategyFactory;

import java.util.Map;
import java.util.Set;

/**
 * Shell command handler for managing configured LLM providers.
 *
 * <p>Exposes subcommands to list configured modules and their assigned providers,
 * and to set the provider for a specific module. This class delegates provider
 * discovery to {@link LlmStrategyFactory} and reads/writes the in-memory module
 * mapping from {@link LlmProperties}.</p>
 *
 * <p>Example commands:
 * <pre>
 * llm list
 * llm set --module classifier --provider groq
 * </pre>
 * </p>
 */
@Command(command = "llm", alias = "l", description = "Manage LLM providers (gemini/groq).")
@RequiredArgsConstructor
public class LlmCommand {

    private final LlmProperties llmProperties;
    private final LlmStrategyFactory llmStrategyFactory;

    /**
     * Lists configured modules and their currently assigned LLM provider.
     *
     * <p>Builds a table with the following columns:
     * <ul>
     * <li>Module name</li>
     * <li>Active provider (module-specific or global default)</li>
     * <li>Available providers (all providers discovered by the factory)</li>
     * </ul>
     * </p>
     *
     * @return A formatted table as a string showing module assignments, or a message
     * stating that no modules are specifically configured (and the global default is used).
     */
    @Command(command = "list", alias = "ls", description = "Show active providers for each module.")
    public String listProviders() {
        Map<String, String> configuredModules = llmProperties.getModules();
        String globalDefault = llmProperties.getDefaultProvider();
        Set<String> availableProviders = llmStrategyFactory.getAvailableProviders();
        String availableStr = String.join(", ", availableProviders);

        if (configuredModules == null || configuredModules.isEmpty()) {
            return "Nessun modulo LLM configurato specificamente (tutti usano default: " + globalDefault + ").";
        }

        // Columns: MODULE | ACTIVE PROVIDER | AVAILABLE
        Object[][] data = new Object[configuredModules.size() + 1][3];
        data[0] = new String[]{"MODULO", "PROVIDER ATTIVO", "DISPONIBILI"};

        int i = 1;
        for (Map.Entry<String, String> entry : configuredModules.entrySet()) {
            String moduleName = entry.getKey();
            String currentProvider = entry.getValue();

            if (currentProvider == null || currentProvider.isBlank()) {
                currentProvider = globalDefault;
            }

            data[i][0] = moduleName;
            data[i][1] = currentProvider;
            data[i][2] = availableStr;
            i++;
        }

        TableModel model = new ArrayTableModel(data);
        TableBuilder tableBuilder = new TableBuilder(model);
        return tableBuilder.addFullBorder(BorderStyle.fancy_light).build().render(80);
    }

    /**
     * Sets the LLM provider for a given module.
     *
     * <p>Validates that the requested provider exists among the beans discovered by
     * {@link LlmStrategyFactory}. Updates the in-memory module mapping in
     * {@link LlmProperties} to the requested provider.</p>
     *
     * @param module   Module identifier (option: --module / -m)
     * @param provider Provider name to set (option: --provider / -p)
     * @return Success message with the previous and new provider, or an error message
     * if the module is not configured or the provider is unavailable.
     */
    @Command(command = "set", description = "Set the LLM provider for a module.")
    public String setProvider(
            @Option(longNames = "module", shortNames = 'm', required = true, description = "Module name (e.g. classifier)") String module,
            @Option(longNames = "provider", shortNames = 'p', required = true, description = "Provider name (e.g. groq)") String provider
    ) {
        Map<String, String> modules = llmProperties.getModules();

        if (!modules.containsKey(module)) {
            return "ERRORE: Modulo '" + module + "' non trovato nella configurazione LLM. Moduli configurati: " + modules.keySet();
        }

        Set<String> availableProviders = llmStrategyFactory.getAvailableProviders();
        if (!availableProviders.contains(provider.toLowerCase())) {
            return "ERRORE: Provider '" + provider + "' non disponibile. Provider validi: " + availableProviders;
        }

        String oldProvider = modules.get(module);
        if (oldProvider == null || oldProvider.isBlank()) {
            oldProvider = llmProperties.getDefaultProvider();
        }

        modules.put(module, provider.toLowerCase());

        return String.format("SUCCESSO: Modulo '%s' aggiornato. Provider cambiato da [%s] a [%s].", module, oldProvider, provider);
    }
}
