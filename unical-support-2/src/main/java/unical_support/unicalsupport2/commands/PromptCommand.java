package unical_support.unicalsupport2.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.validation.annotation.Validated;
import unical_support.unicalsupport2.configurations.factory.PromptProperties;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;

import java.util.Map;

@Validated
@Command(command = "prompt", alias = "p", description = "Gestione strategia di prompting.")
@RequiredArgsConstructor
public class PromptCommand {

    /**
     * Configuration holder for prompt templates and strategies.
     * Injected via constructor.
     */
    private final PromptProperties promptProperties;

    /**
     * List configured modules and their active/default strategy.
     *
     * <p>Builds a simple table showing:
     * <ul>
     *   <li>module name</li>
     *   <li>active/default strategy for that module</li>
     *   <li>available strategies (comma-separated)</li>
     * </ul>
     * If no modules are configured the method returns an informational message.</p>
     *
     * @return a formatted table as a string or an informational message when no modules exist
     */
    @Command(command = "list", alias = "ls", description = "Mostra le strategie attive per ogni modulo.")
    public String listStrategies() {
        Map<String, PromptProperties.ModuleConfig> modules = promptProperties.getModules();

        if (modules == null || modules.isEmpty()) {
            return "Nessun modulo configurato.";
        }

        // Creazione tabella per output formattato
        Object[][] data = new Object[modules.size() + 1][3];
        data[0] = new String[]{"MODULO", "STRATEGIA ATTIVA", "DISPONIBILI"};

        int i = 1;
        for (Map.Entry<String, PromptProperties.ModuleConfig> entry : modules.entrySet()) {
            String moduleName = entry.getKey();
            PromptProperties.ModuleConfig config = entry.getValue();

            String active = config.getDefaultStrategy();
            String available = String.join(", ", config.getStrategies().keySet());

            data[i][0] = moduleName;
            data[i][1] = active;
            data[i][2] = available;
            i++;
        }

        TableModel model = new ArrayTableModel(data);
        TableBuilder tableBuilder = new TableBuilder(model);
        return tableBuilder.addFullBorder(BorderStyle.fancy_light).build().render(80);
    }

    /**
     * Set the default strategy for a given module.
     *
     * <p>Validates that the specified module exists and that the requested strategy
     * is defined for that module. If validation passes, updates the module's
     * default strategy and returns a success message indicating the previous and
     * new strategy names. On validation failure an error message is returned.</p>
     *
     * @param module   the module identifier (option: --module / -m), required
     * @param strategy the strategy name to set as default (option: --strategy / -s), required
     * @return success message when updated or an error message when validation fails
     */
    @Command(command = "set", description = "Imposta la strategia di default per un modulo.")
    public String setStrategy(
            @Option(longNames = "module", shortNames = 'm', required = true, description = "Nome del modulo (es. classifier)") String module,
            @Option(longNames = "strategy", shortNames = 's', required = true, description = "Nome della strategia (es. fewShot)") String strategy
    ) {

        var modules = promptProperties.getModules();
        if (!modules.containsKey(module)) {
            return "ERRORE: Modulo '" + module + "' non trovato. Moduli validi: " + modules.keySet();
        }

        var moduleConfig = modules.get(module);
        if (!moduleConfig.getStrategies().containsKey(strategy)) {
            return "ERRORE: Strategia '" + strategy + "' non definita per il modulo '" + module + "'. Strategie valide: " + moduleConfig.getStrategies().keySet();
        }

        String oldStrategy = moduleConfig.getDefaultStrategy();
        moduleConfig.setDefaultStrategy(strategy);

        return String.format("SUCCESSO: Modulo '%s' aggiornato. Strategia cambiata da [%s] a [%s].", module, oldStrategy, strategy);
    }
}
