package unical_support.unicalsupport2.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.validation.annotation.Validated;
import unical_support.unicalsupport2.data.enumerators.PromptStrategyName;
import unical_support.unicalsupport2.prompting.PromptService;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidStrategyName;

import java.util.Arrays;

@Validated
@Command(command = "prompt", alias = "p", description = "Gestione strategia di prompting.")
@RequiredArgsConstructor
public class PromptCommand {

    private final PromptService promptService;

    @Command(command = "list", alias = "ls", description = "Elenca le strategie disponibili.")
    public void list() {
        Arrays.stream(PromptStrategyName.values())
                .forEach(value -> System.out.println(value.name() + " or " + value.getBeanName()));
        System.out.println();
    }

    @Command(command = "set", alias = "s", description = "Cambia la strategia di prompting.")
    public String setPromptStrategy(
            @ValidStrategyName
            @Option(longNames = "strategy", shortNames = 's')
            String strategyType
    ) {
        promptService.setCurrentStrategy(strategyType);
        return "Strategia di prompting impostata su: " + strategyType;
    }

    @Command(command = "current", alias = "c", description = "Mostra la strategia di prompting attuale.")
    public String getPromptStrategy() {
        return "Strategia corrente: " + promptService.getCurrentStrategy();
    }
}
