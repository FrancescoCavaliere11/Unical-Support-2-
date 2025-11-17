package unical_support.unicalsupport2.prompting.implementations;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.classifier.SingleCategoryDto;
import unical_support.unicalsupport2.data.entities.Template;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.data.repositories.TemplateRepository;
import unical_support.unicalsupport2.prompting.PromptStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("fewShot")
@RequiredArgsConstructor
public class FewShotPromptStrategy implements PromptStrategy {

    private final CategoryRepository categoryRepository;
    private final ObjectMapper mapper = new ObjectMapper();
    private final TemplateRepository templateRepository;

    // ==========================
    // 1) PROMPT PER CLASSIFIER
    // ==========================
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
                .append("Non aggiungere testo fuori dal JSON, né markdown, né commenti.\n\n")
                .append("Ecco alcuni esempi di INPUT e OUTPUT nel formato corretto:\n\n")

                .append("Esempio 1 - INPUT:\n")
                .append("Contenuto email:\n")
                .append("Buongiorno,\n")
                .append("durante l’ultimo appello scritto di Economia Aziendale si sono verificati problemi tecnici che hanno impedito ad alcuni studenti di completare la prova.\n")
                .append("Vorrei segnalare l’accaduto e chiedere se è previsto un appello straordinario.\n")
                .append("Grazie per l’attenzione.\n\n")
                .append("Esempio 1 - OUTPUT JSON:\n")
                .append("[\n")
                .append("  {\n")
                .append("    \"id\": 0,\n")
                .append("    \"categories\": [\n")
                .append("      { \"name\": \"Reclamo\", \"confidence\": 0.85, \"text\": \"si sono verificati problemi tecnici che hanno impedito ad alcuni studenti di completare la prova\" },\n")
                .append("      { \"name\": \"Esami e appelli\", \"confidence\": 0.75, \"text\": \"durante l’ultimo appello scritto di Economia Aziendale\" }\n")
                .append("    ],\n")
                .append("    \"explanation\": \"Segnalazione di un disservizio durante un esame, quindi reclamo e categoria esami e appelli.\"\n")
                .append("  }\n")
                .append("]\n\n")

                .append("Esempio 2 - INPUT:\n")
                .append("Contenuto email:\n")
                .append("SALVE vorrei avere informazioni se è possibile laurearsi a dicembre oppure devo aspettare all'anno prossimo, grazie per l'attenzione \n")
                .append("Cordiali saluti LP\n\n")
                .append("Esempio 2 - OUTPUT JSON:\n")
                .append("[\n")
                .append("  {\n")
                .append("    \"id\": 0,\n")
                .append("    \"categories\": [\n")
                .append("      { \"name\": \"Laurea e tesi\", \"confidence\": 0.95, \"text\": \"vorrei avere informazioni se è possibile laurearsi a dicembre oppure devo aspettare all'anno prossimo\" }\n")
                .append("    ],\n")
                .append("    \"explanation\": \"Richiesta di informazioni sulla sessione di laurea.\"\n")
                .append("  }\n")
                .append("]\n\n")

                .append("Ora classifica le seguenti email nello stesso formato JSON, SENZA AGGIUNGERE MARKDOWN O COMMENTI:\n\n");

        for (int i = 0; i < classificationEmailDtos.size(); i++) {
            ClassificationEmailDto e = classificationEmailDtos.get(i);
            prompt.append("ID: ").append(i).append("\n")
                    .append("OGGETTO: ").append(e.getSubject()).append("\n")
                    .append("CORPO: ").append(e.getBody()).append("\n\n");
        }

        return prompt.toString();
    }


    // PROMPT PER JUDGER

    @Override
    public String buildJudgePrompt(List<ClassificationEmailDto> emails,
                                   List<ClassificationResultDto> results) {

        StringBuilder prompt = new StringBuilder();

        // ==== PARTE "SYSTEM" ====
        prompt.append("""
            Sei un modello di revisione esperto che valuta classificazioni generate da un altro modello di intelligenza artificiale per una segreteria universitaria.

            Per ogni email ricevi:
            - OGGETTO e CORPO dell'email;
            - CATEGORIE_PROPOSTE: array JSON con oggetti { "category", "confidence", "text" } generati dal classificatore;
            - SPIEGAZIONE DEL CLASSIFICATORE.

            Il tuo compito è:

            1. Per OGNI categoria proposta:
               - verifica se la categoria è coerente con il contenuto dell'email;
               - assegna un punteggio "confidence" tra 0 e 1:
                    * vicino a 0  => categoria sostanzialmente ERRATA;
                    * intorno a 0.5 => categoria AMBIGUA o poco supportata;
                    * vicino a 1  => categoria chiaramente CORRETTA;
               - scrivi una breve "explanation" formale in italiano che spieghi perché il punteggio è alto, medio o basso;
               - imposta anche un campo "verdict" con uno dei seguenti valori:
                    * "CORRETTO"  se la categoria è chiaramente supportata dal testo dell'email;
                    * "AMBIGUO"   se la categoria è solo parzialmente o debolmente supportata;
                    * "ERRATO"    se la categoria non è supportata dal testo o è fuorviante.

            2. Per l'intera email:
               - fornisci un "overallConfidence" (0–1) che indica quanto, nel complesso,
                 la classificazione proposta è affidabile;
               - scrivi una breve "summary" formale in italiano che riassuma il tuo giudizio.

            FORMATO DI RISPOSTA OBBLIGATORIO (SOLO JSON, SENZA TESTO ESTERNO):

            [
              {
                "id": number,  // stesso ID dell'email in input
                "categoriesEvaluation": [
                  {
                    "category": "string",
                    "confidence": number,
                    "explanation": "string",
                    "verdict": "CORRETTO" | "AMBIGUO" | "ERRATO"
                  }
                ],
                "overallConfidence": number,
                "summary": "string"
              }
            ]

            Regole IMPORTANTI:
            - Rispondi SOLO con il JSON array sopra descritto.
            - NON inserire markdown, NON usare ```json, NON usare backtick.
            - NON scrivere testo prima o dopo il JSON.
            - Mantieni esattamente gli ID che ti vengono forniti.

            Ora valuta le seguenti email e le rispettive classificazioni proposte.
            Ricorda: restituisci SOLO il JSON array finale.SENZA MARKDOWN O COMMENTI
            """);

        // ==== PARTE "USER": elenco email + classificazioni ====
        prompt.append("\n\nEMAIL E CLASSIFICAZIONI DA VALUTARE:\n\n");

        for (int i = 0; i < emails.size(); i++) {
            ClassificationEmailDto email = emails.get(i);
            ClassificationResultDto classification = results.get(i);

            String categoriesJson;
            try {
                categoriesJson = mapper.writeValueAsString(classification.getCategories());
            } catch (Exception ex) {
                categoriesJson = "[]";
            }

            prompt.append("ID: ").append(i).append("\n")
                    .append("OGGETTO: ").append(ns(email.getSubject())).append("\n")
                    .append("CORPO: ").append(ns(email.getBody())).append("\n")
                    .append("CATEGORIE_PROPOSTE:\n")
                    .append(categoriesJson).append("\n")
                    .append("SPIEGAZIONE DEL CLASSIFICATORE:\n")
                    .append(ns(classification.getExplanation())).append("\n\n");
        }

        prompt.append("Restituisci ORA soltanto il JSON array, senza alcun testo esterno. SENZA NESSUN MARKDOWN E ALTRO");

        return prompt.toString();
    }

    private String ns(String s) {
        return s == null ? "" : s;
    }

    @Override
    public String buildResponderPrompt(List<ClassificationResultDto> emails) {
        // todo aggiungere il few shot
        StringBuilder sb = new StringBuilder();
        sb.append("""
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
            """
        );

        Map<String, List<Template>> templatesCache = new HashMap<>();

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
