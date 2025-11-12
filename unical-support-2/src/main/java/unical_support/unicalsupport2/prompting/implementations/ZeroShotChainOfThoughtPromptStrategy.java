package unical_support.unicalsupport2.prompting.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import unical_support.unicalsupport2.data.dto.ClassificationEmailDto;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.prompting.PromptStrategy;

import java.util.List;

@Component("zeroShotCoT")
@RequiredArgsConstructor
public class ZeroShotChainOfThoughtPromptStrategy implements PromptStrategy {

    private final CategoryRepository categoryRepository;

    @Override
    public String buildClassifyPrompt(List<ClassificationEmailDto> classificationEmailDtos) {
        List<String> categories = categoryRepository.findAll()
                .stream()
                .map(cat -> "name: \"" + cat.getName() + "\", description: \"" + cat.getDescription() + "\"")
                .toList();

        StringBuilder prompt = new StringBuilder();

        prompt.append("Sei un classificatore di email per una segreteria universitaria.\n")
                .append("Devi assegnare UNA o PIÙ categorie per ciascuna email tra le seguenti:\n")
                .append(String.join(", ", categories))
                .append("\n\n")
                .append("Ragiona passo per passo per ciascuna email:\n")
                .append("1. Analizza il contenuto e individua le parole chiave rilevanti.\n")
                .append("2. Rifletti sullo scopo del messaggio e sull'intento del mittente.\n")
                .append("3. Identifica le categorie più appropriate.\n")
                .append("4. Fornisci un livello di confidenza (0–1) e il testo giustificativo.\n")
                .append("5. Scrivi una breve spiegazione del tuo ragionamento.\n\n")
                .append("Rispondi SOLO con un JSON array nel seguente formato:\n")
                .append("[\n")
                .append("  {\n")
                .append("    \"id\": number,\n")
                .append("    \"categories\": [\n")
                .append("      { \"name\": string, \"confidence\": number, \"text\": string }\n")
                .append("    ],\n")
                .append("    \"explanation\": string\n")
                .append("  }\n")
                .append("]\n\n")
                .append("Non aggiungere testo fuori dal JSON.\n\n")
                .append("Analizza e classifica le seguenti email passo per passo:\n\n");

        for (int i = 0; i < classificationEmailDtos.size(); i++) {
            ClassificationEmailDto e = classificationEmailDtos.get(i);
            prompt.append("ID: ").append(i).append("\n")
                    .append("OGGETTO: ").append(e.getSubject()).append("\n")
                    .append("CORPO: ").append(e.getBody()).append("\n")
                    .append("Ragiona passo per passo e poi restituisci solo il JSON.\n\n");
        }

        return prompt.toString();
    }
}
