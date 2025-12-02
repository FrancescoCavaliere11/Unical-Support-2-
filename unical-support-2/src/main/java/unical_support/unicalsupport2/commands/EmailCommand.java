package unical_support.unicalsupport2.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import unical_support.unicalsupport2.service.interfaces.OrchestratorService;

/**
 * Spring Shell command class to fetch, classify and forward emails.
 *
 * <p>Usage:</p>
 * <pre>
 * s f              -> Modalità Batch (Default)
 * s f --sequential -> Modalità Sequenziale (1 per volta)
 * s f -q           -> Alias per sequenziale
 * </pre>
 */
@Command(command = "start", alias = "s", description = "Commands for start email fetching and classification")
@RequiredArgsConstructor
public class EmailCommand {

    private final OrchestratorService orchestratorService;

    @Command(command = "fetch", alias = "f", description = "Fetch emails from the server")
    public void fetchEmailAndClassify(
            @Option(longNames = "sequential", shortNames = 'q', defaultValue = "false", description = "Process emails one by one instead of batch")
            boolean sequential
    ) {
        orchestratorService.start(sequential);
    }
}