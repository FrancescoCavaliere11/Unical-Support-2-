package unical_support.unicalsupport2.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.validation.annotation.Validated;
import unical_support.unicalsupport2.data.enumerators.PromptStrategyName;
import unical_support.unicalsupport2.prompting.PromptService;

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

}
