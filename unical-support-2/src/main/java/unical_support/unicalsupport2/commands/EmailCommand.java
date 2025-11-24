package unical_support.unicalsupport2.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import unical_support.unicalsupport2.service.interfaces.*;

/**
 * Spring Shell command class to fetch, classify and forward emails.
 *
 * <p>Provides a CLI entry point to start the application flow
 */

@Command(command = "start" , alias = "s", description = "Commands for start email fetching and classification")
@RequiredArgsConstructor
public class EmailCommand {
    private final OrchestratorService orchestratorService;

    @Command(command = "fetch", alias = "f", description = "Fetch emails from the server")
    public void fetchEmailAndClassify() {
        orchestratorService.start();
    }

}
