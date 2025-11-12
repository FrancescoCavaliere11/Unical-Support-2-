package unical_support.unicalsupport2.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.classifier.SingleCategoryDto;
import unical_support.unicalsupport2.data.entities.Template;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.data.repositories.TemplateRepository;
import unical_support.unicalsupport2.service.interfaces.PromptService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PromptServiceImpl implements PromptService {

    private final CategoryRepository categoryRepository;
    private final TemplateRepository templateRepository;

    @Override
    public String buildSystemMessageBatch() {
        List<String> categories = categoryRepository.findAll()
                .stream()
                .map(cat -> "name: \"" + cat.getName() + "\", description: \"" + cat.getDescription() + "\"")
                .toList();

        return "Sei un classificatore di email per una segreteria universitaria.\n" +
                "Puoi assegnare UNA o PIÙ categorie a ciascuna email.\n" +
                "Per ogni categoria assegnata, devi fornire:\n" +
                " - CONFIDENCE (numero tra 0 e 1): quanto sei sicuro che quella categoria sia appropriata.\n" +
                " - TEXT: copia ESATTAMENTE la porzione di testo dall'email che giustifica quella categoria.\n" +
                "Scrivi UNA sola spiegazione per ciascuna email, che motivi tutte le categorie scelte.\n" +
                "Restituisci SOLO un JSON array, senza testo extra, senza markdown.\n" +
                "Ogni elemento dell'array deve avere questo schema:\n" +
                "{\n" +
                "  \"id\": number,     // ID dell'email fornito nell'input\n" +
                "  \"categories\": [   // elenco di categorie riconosciute\n" +
                "     {\n" +
                "       \"name\": string,\n" +
                "       \"confidence\": number,     // numero tra 0 e 1\n" +
                "       \"text\": string            // testo estratto dall'email\n" +
                "     }\n" +
                "  ],\n" +
                "  \"explanation\": string   // breve spiegazione complessiva in italiano\n" +
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

    @Override
    public String buildSystemMessageResponse() {
        return """
        Sei un assistente AI che genera risposte email per una segreteria universitaria.
        
        OBIETTIVO
        Riceverai:
        - Una lista di email
        - Per ogni email: una lista di categorie già classificate (con confidence e testo estratto)
        - Una lista di template disponibili
        
        Per OGNI email:
        - Per OGNI categoria assegnata all'email:
          - Determina se esiste un template corrispondente
          - Se sì, compila il template sostituendo {{parametri}} usando il testo dell’email e/o della categoria
          - Se no, segnala che non esiste un template applicabile
         
        
        STRUTTURA OUTPUT OBBLIGATORIA
        Devi restituire SOLO un array JSON, uno per ogni email, nello stesso ordine fornito.
        
        Formato di ogni elemento dell'array:
        
        {
          "email_id": number,
          "responses": [
            {
              "category": "nome_categoria",
              "template": "nome_template" | null,
              "content": "testo compilato" | null,
              "parameters": {
                "param1": "valore" | null,
                "param2": "valore" | null
              },
              "reason": "OK" | "NO_TEMPLATE_MATCH" | "MISSING_REQUIRED_PARAMETER"
            }
          ]
        }
        
        IL CAMPO email_id DEVE corrispondere all'id dell'email nell'input.
        Devi cercare di riempire tutti i parametri con required = true dei template, altrimenti metti a NULL il parametro e metti MISSING_REQUIRED_PARAMETER NEL CAMPO reason, seguito da tutti i parametri mancanti separati da virgola. Se riesci riempi anche i parametri non required.
        REGOLE IMPORTANTI
        - SEMPRE restituisci una risposta per ogni categoria dell'email
        - I parametri possono essere estratti:
          - dal testo della mail, oppure
          - dal testo associato alla categoria
        - NON inventare parametri che il template non contiene
        - Se un parametro richiesto non può essere estratto → mettilo a null
        - Non aggiungere testo fuori dal JSON
        - Nessun commento, nessun markdown
        - Ordine dell’output = ordine input email
        
        ESEMPI SEMANTICI (NON REALI)
        
        [
          {
            "email_id": 0,
            "responses": [
              {
                "category": "IMMATRICOLAZIONE",
                "template": "RICHIESTA_IMMATRICOLAZIONE",
                "content": "Gentile Mario, per immatricolarti devi...",
                "parameters": { "nome": "Mario" },
                "reason": "OK"
              },
              {
                "category": "TASSE",
                "template": null,
                "content": null,
                "parameters": {},
                "reason": "NO_TEMPLATE_MATCH"
              }
            ]
          }
        ]
        
        Fine istruzioni.
        """;
    }


    @Override
    public String buildUserMessageResponse(List<ClassificationResultDto> emails) {
        Map<String, List<Template>> templatesCache = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        sb.append("Di seguito le email classificate e i template disponibili.\n");
        sb.append("Genera la risposta nel formato JSON richiesto.\n\n");

        for (ClassificationResultDto email : emails) {
            sb.append("EMAIL ID: ").append(email.getId()).append("\n");
            sb.append("CATEGORIE:\n");

            for (SingleCategoryDto category : email.getCategories()) {
                String categoryName = category.getCategory();

                List<Template> templates = templatesCache.computeIfAbsent(
                        categoryName,
                        k -> templateRepository.findByCategoryNameIgnoreCase(categoryName)
                );

                sb.append(" - Categoria: ").append(category.getCategory()).append("\n");
                sb.append("   Confidence: ").append(category.getConfidence()).append("\n");
                sb.append("   Testo: ").append(category.getText()).append("\n");

                if (!templates.isEmpty()) {
                    sb.append("   Template disponibili:\n");
                    for (Template t : templates) {
                        sb.append("     * Nome: ").append(t.getName()).append("\n");
                        sb.append("       Parametri: ").append(
                                t.getParameters().stream()
                                        .map(p -> p.getName() + " (required=" + p.isRequired() + ")")
                                        .toList()
                        ).append("\n");
                        sb.append("       Contenuto template: ").append(
                                t.getContent().replace("\n", " ").replace("\r", "")
                        ).append("\n");
                    }
                } else {
                    sb.append("   Template disponibili: nessuno\n");
                }
            }
            sb.append("\n");
        }

        sb.append("Genera ora SOLO l'array JSON di risposta, seguendo ESATTAMENTE il formato definito nel system prompt, senza testo aggiuntivo.\n");

        return sb.toString();
    }

}