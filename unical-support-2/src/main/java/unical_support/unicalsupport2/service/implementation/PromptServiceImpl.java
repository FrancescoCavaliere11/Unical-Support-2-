package unical_support.unicalsupport2.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.data.dto.ClassificationEmailDto;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.service.interfaces.PromptService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromptServiceImpl implements PromptService {

    private final CategoryRepository categoryRepository;

    @Override
    public String buildSystemMessageBatch() {
        List<String> categories = categoryRepository.findAll()
                .stream()
                .map(cat -> "\"" + cat.getName() + "\"")
                .toList();

        return "Sei un classificatore di email per una segreteria universitaria.\n" +
                "Puoi assegnare UNA o PIÙ categorie a ciascuna email.\n" +
                "Scrivi UNA sola spiegazione per ciascuna email, che motivi tutte le categorie scelte.\n" +
                "Restituisci SOLO un JSON array, senza testo extra, senza markdown.\n" +
                "Ogni elemento dell'array deve avere questo schema:\n" +
                "{\n" +
                "  \"id\": number,     // ID dell'email fornito nell'input\n" +
                "  \"categories\": [   // elenco di categorie riconosciute\n" +
                "     {\"name\": string, \"confidence\": number}   // tra 0 e 1\n" +
                "  ],\n" +
                "  \"explanation\": string   // breve spiegazione in italiano\n" +
                "}\n\n" +
                "Categorie possibili: " + String.join(", ", categories) + "\n\n" +
                "Non aggiungere testo fuori dal JSON.\n";
    }

    @Override
    public String buildUserMessageBatch(List<ClassificationEmailDto> classificationEmailDtos) {
        StringBuilder sb = new StringBuilder();
        sb.append("Classifica le seguenti email. Rispondi con un JSON array come descritto.\n\n");
        for (int i = 0; i < classificationEmailDtos.size(); i++) {
            ClassificationEmailDto e = classificationEmailDtos.get(i);
            sb.append("ID: ").append(i).append("\n");
            sb.append("OGGETTO: ").append(e.getSubject()).append("\n");
            sb.append("CORPO: ").append(e.getBody()).append("\n\n");
        }
        return sb.toString();
    }
}