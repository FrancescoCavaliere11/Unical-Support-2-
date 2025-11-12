package unical_support.unicalsupport2.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import unical_support.unicalsupport2.prompting.PromptService;

@Command(command = "prompt", alias = "p", description = "Gestione strategia di prompting.")
@RequiredArgsConstructor
public class PromptCommand {

    private final PromptService promptService;

    @Command(command = "set", alias = "s", description = "Cambia la strategia di prompting.")
    public String setPromptStrategy(
            @Option(longNames = "strategy", shortNames = 's') String strategyType
    ) {
        promptService.setCurrentStrategy(strategyType);
        return "Strategia di prompting impostata su: " + strategyType;
    }

    @Command(command = "current", alias = "c", description = "Mostra la strategia di prompting attuale.")
    public String getPromptStrategy() {
        return "Strategia corrente: " + promptService.getCurrentStrategy();
    }
}
