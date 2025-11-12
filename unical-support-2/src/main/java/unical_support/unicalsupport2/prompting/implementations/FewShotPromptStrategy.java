package unical_support.unicalsupport2.prompting.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import unical_support.unicalsupport2.data.dto.ClassificationEmailDto;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.prompting.PromptStrategy;

import java.util.List;

@Component("fewShot")
@RequiredArgsConstructor
public class FewShotPromptStrategy implements PromptStrategy {

    private final CategoryRepository categoryRepository;

    @Override
    public String buildClassifyPrompt(List<ClassificationEmailDto> classificationEmailDtos) {
        List<String> categories = categoryRepository.findAll()
                .stream()
                .map(cat -> "name: \"" + cat.getName() + "\", description: \"" + cat.getDescription() + "\"")
                .toList();

        StringBuilder prompt = new StringBuilder();

        prompt.append("Sei un classificatore di email per una segreteria universitaria.\n")
                .append("Analizza il contenuto di ogni email e assegna UNA o PIÙ categorie tra le seguenti:\n")
                .append(String.join(", ", categories))
                .append("\n\n")
                .append("Per ogni email devi restituire un JSON con:\n")
                .append(" - id (numero progressivo)\n")
                .append(" - categories (lista di oggetti con nome, confidenza e testo estratto)\n")
                .append(" - explanation (spiegazione sintetica della classificazione)\n\n")
                .append("Restituisci SOLO un JSON array nel formato seguente:\n")
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
                .append("Ecco alcuni esempi:\n\n")

                .append("Esempio 1:\n")
                .append("Contenuto email:\n")
                .append("Buongiorno,\n")
                .append("durante l’ultimo appello scritto di Economia Aziendale si sono verificati problemi tecnici che hanno impedito ad alcuni studenti di completare la prova.\n")
                .append("Vorrei segnalare l’accaduto e chiedere se è previsto un appello straordinario.\n")
                .append("Grazie per l’attenzione.\n\n")
                .append("Risposta:\n")
                .append("Categories=[name=Reclamo, confidence=0.85, text=durante l’ultimo appello scritto di Economia Aziendale si sono verificati problemi tecnici che hanno impedito ad alcuni studenti di completare la prova., ")
                .append("name=Esami e appelli, confidence=0.75, text=durante l’ultimo appello scritto di Economia Aziendale si sono verificati problemi tecnici che hanno impedito ad alcuni studenti di completare la prova.], ")
                .append("explanation=Segnalazione di un disservizio durante un esame, quindi reclamo e relativo a esami.\n\n")

                .append("Esempio 2:\n")
                .append("Contenuto email:\n")
                .append("SALVE vorrei avere informazioni se è possibile laurearsi a dicembre oppure devo aspettare all'anno prossimo, grazie per l'attenzione \n")
                .append("Cordiali saluti LP\n\n")
                .append("Risposta:\n")
                .append("Categories=[name=Laurea e tesi, confidence=0.95, text=vorrei avere informazioni se è possibile laurearsi a dicembre oppure devo aspettare all'anno prossimo], ")
                .append("explanation=Richiesta di informazioni sulla sessione di laurea.\n\n")

                .append("Ora classifica le seguenti email nello stesso formato:\n\n");

        for (int i = 0; i < classificationEmailDtos.size(); i++) {
            ClassificationEmailDto e = classificationEmailDtos.get(i);
            prompt.append("ID: ").append(i).append("\n")
                    .append("OGGETTO: ").append(e.getSubject()).append("\n")
                    .append("CORPO: ").append(e.getBody()).append("\n\n");
        }

        return prompt.toString();
    }
}
