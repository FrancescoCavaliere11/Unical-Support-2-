package unical_support.unicalsupport2.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.validation.annotation.Validated;
import unical_support.unicalsupport2.data.enumerators.PromptStrategyName;
import unical_support.unicalsupport2.prompting.PromptService;
import org.springframework.shell.command.annotation.Option;


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

    private String normalizeStrategy(String strategy) {
        if (strategy == null) return null;
        String s = strategy.trim().toLowerCase();

        // alias "brevi"
        if (s.equals("few") || s.equals("fewshot")) {
            return PromptStrategyName.FEW_SHOT.getBeanName(); // "fewShot"
        }
        if (s.equals("zero") || s.equals("zeroshot") || s.equals("cot") || s.equals("zeroshotcot")) {
            return PromptStrategyName.ZERO_SHOT_CHAIN_OF_THOUGHT.getBeanName(); // "zeroShotCoT"
        }

        // prova a matchare con enum name
        for (PromptStrategyName value : PromptStrategyName.values()) {
            if (value.name().equalsIgnoreCase(s) || value.getBeanName().equalsIgnoreCase(s)) {
                return value.getBeanName();
            }
        }

        return null;
    }

    @Command(command = "set", alias = "s",
            description = "Imposta la strategia di prompting per un modulo (classify|judge|responder).")
    public String set(
            @Option(longNames = "module", shortNames = 'm',
                    description = "Modulo: classify|judge|responder") String module,
            @Option(longNames = "strategy", shortNames = 's',
                    description = "Strategia: few|fewShot|zeroShot|cot") String strategy
    ) {
        if (module == null || strategy == null) {
            return "Errore: specifica sia --module (-m) che --strategy (-s).";
        }

        String normalizedModule = normalizeModule(module);
        if (normalizedModule == null) {
            return "Modulo non valido. Usa: classify | judge | responder.";
        }

        String beanName = normalizeStrategy(strategy);
        if (beanName == null) {
            return "Strategia non valida. Strategie supportate: few, fewShot, zeroShot, cot.";
        }

        switch (normalizedModule) {
            case "classifier":
                promptService.setClassifyCurrentStrategy(beanName);
                break;
            case "judger":
                promptService.setJudgeCurrentStrategy(beanName);
                break;
            case "responder":
                promptService.setResponderCurrentStrategy(beanName);
                break;
            default:
                return "Errore interno: modulo non riconosciuto.";
        }

        return "Strategia di prompting per il modulo '" + normalizedModule +
                "' impostata a runtime su: " + beanName;
    }


    private String normalizeModule(String module) {
        if (module == null) return null;
        String m = module.trim().toLowerCase();

        switch (m) {
            case "classify":
            case "classifier":
                return "classifier";
            case "judge":
            case "judger":
                return "judger";
            case "respond":
            case "responder":
                return "responder";
            default:
                return null;
        }
    }


}
