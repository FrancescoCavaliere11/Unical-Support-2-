package unical_support.unicalsupport2.service.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.configurations.LlmClientFactory;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.judger.CategoryEvaluationDto;
import unical_support.unicalsupport2.data.dto.judger.JudgementResultDto;
import unical_support.unicalsupport2.prompting.PromptService;
import unical_support.unicalsupport2.runtime.ActiveJudgerLlmRegistry;
import unical_support.unicalsupport2.service.interfaces.JudgerService;
import unical_support.unicalsupport2.service.interfaces.LlmClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JudgerServiceImpl implements JudgerService {

    private final PromptService promptService;

    private final ActiveJudgerLlmRegistry judgerRegistry;


    private final LlmClientFactory llmClientFactory;

    private final GeminiApiClientImpl geminiApiClient;
    private final GroqLlmClient groqLlmClient;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<JudgementResultDto> judge(List<ClassificationEmailDto> emails,
                                          List<ClassificationResultDto> results) {

        if (emails == null || results == null || emails.size() != results.size()) {
            log.warn("Judge: input size mismatch (emails={}, results={})",
                    emails == null ? null : emails.size(),
                    results == null ? null : results.size());
            return errorFallback(emails, "Input non valido (size mismatch).");
        }

        try {

            final String prompt = promptService.buildJudgePrompt(emails, results);


            LlmClient client = selectJudgerClient();
            String raw = client.chat(prompt);

            log.debug("Judger RAW response: {}", raw);


            String cleaned = sanitizeJson(raw);


            ArrayNode arr = (ArrayNode) mapper.readTree(cleaned);


            List<JudgementResultDto> out = new ArrayList<>(Collections.nCopies(
                    emails.size(),
                    emptyItem()
            ));

            for (JsonNode n : arr) {
                int id = n.path("id").asInt(-1);
                if (id < 0 || id >= emails.size()) {
                    log.warn("Judge: elemento con id={} fuori range, ignorato.", id);
                    continue;
                }
                out.set(id, parseItem(n, id));
            }


            for (int i = 0; i < out.size(); i++) {
                if (out.get(i) == null || out.get(i).getCategoriesEvaluation() == null) {
                    out.set(i, errorItem(i, "Risposta mancante o non valida dal judger per questo ID."));
                }
            }

            return out;

        } catch (Exception ex) {
            log.error("Judge: errore durante la validazione batch: {}", ex.getMessage(), ex);
            return errorFallback(emails, "Errore judger/API: " + ex.getMessage());
        }
    }

    /**
     * Se esiste un provider dedicato nel registry, usa quello;
     * altrimenti usa il client corrente della LlmClientFactory.
     */
    private LlmClient selectJudgerClient() {
        String provider = judgerRegistry.get();
        if (provider == null || provider.isBlank()) {
            LlmClient c = llmClientFactory.current();
            log.info("Judger: uso client di fallback dal factory ({}).",
                    c.getClass().getSimpleName());
            return c;
        }

        switch (provider.toLowerCase()) {
            case "gemini" -> {
                log.info("Judger: uso provider GEMINI.");
                return geminiApiClient;
            }
            case "groq" -> {
                log.info("Judger: uso provider GROQ.");
                return groqLlmClient;
            }
            default -> {
                log.warn("Judger: provider '{}' non riconosciuto, fallback a factory.current().", provider);
                return llmClientFactory.current();
            }
        }
    }

    /**
     * Parsing di UNA email (un oggetto dell'array JSON del judger).
     */
    private JudgementResultDto parseItem(JsonNode n, int id) {
        JudgementResultDto dto = new JudgementResultDto();
        dto.setId(id);

        // categoriesEvaluation
        List<CategoryEvaluationDto> evals = new ArrayList<>();
        JsonNode arr = n.path("categoriesEvaluation");
        if (arr.isArray()) {
            for (JsonNode e : arr) {
                String category = safe(e.path("category").asText());
                Double conf = e.path("confidence").isNumber()
                        ? clamp01(e.path("confidence").asDouble())
                        : null;
                String explanation = safe(e.path("explanation").asText());
                String verdict = safe(e.path("verdict").asText());   // <-- nuovo campo

                CategoryEvaluationDto ce = new CategoryEvaluationDto();
                ce.setCategory(category);
                ce.setConfidence(conf);
                ce.setExplanation(explanation);
                ce.setVerdict(verdict);
                evals.add(ce);
            }
        }
        dto.setCategoriesEvaluation(evals);

        // overallConfidence
        Double overall = n.path("overallConfidence").isNumber()
                ? clamp01(n.path("overallConfidence").asDouble())
                : null;
        dto.setOverallConfidence(overall);

        // summary
        dto.setSummary(safe(n.path("summary").asText()));

        return dto;
    }

    private List<JudgementResultDto> emptyFallback(List<ClassificationEmailDto> emails) {
        int size = emails == null ? 0 : emails.size();
        List<JudgementResultDto> out = new ArrayList<>(size);
        for (int i = 0; i < size; i++) out.add(emptyItem(i));
        return out;
    }

    private List<JudgementResultDto> errorFallback(List<ClassificationEmailDto> emails, String msg) {
        int size = emails == null ? 0 : emails.size();
        List<JudgementResultDto> out = new ArrayList<>(size);
        for (int i = 0; i < size; i++) out.add(errorItem(i, msg));
        return out;
    }

    private JudgementResultDto emptyFallbackItem() {
        return new JudgementResultDto(
                null,
                List.of(),
                null,
                "Nessun giudizio disponibile."
        );
    }

    private JudgementResultDto emptyItem(int id) {
        return new JudgementResultDto(
                id,
                List.of(),
                null,
                "Nessun giudizio disponibile."
        );
    }

    private JudgementResultDto errorItem(int id, String msg) {
        return new JudgementResultDto(
                id,
                List.of(),
                0.0,
                msg
        );
    }

    private JudgementResultDto emptyItem() {
        return emptyFallbackItem();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }

    /**
     * Rende la risposta del modello parsabile come JSON "pulito".
     * - Rimuove eventuali '''
     * - Se ci sono caratteri prima di '[' o '{', taglia fino al primo di questi.
     */
    private String sanitizeJson(String raw) {
        if (raw == null) return "[]";
        String s = raw.trim();


        if (s.startsWith("```")) {
            int firstNewline = s.indexOf('\n');
            if (firstNewline > 0) {
                s = s.substring(firstNewline + 1);
            } else {
                s = s.substring(3);
            }
            int lastFence = s.lastIndexOf("```");
            if (lastFence >= 0) {
                s = s.substring(0, lastFence);
            }
            s = s.trim();
        }
        int start = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '[' || c == '{') {
                start = i;
                break;
            }
        }
        if (start > 0) {
            s = s.substring(start);
        }

        return s.trim();
    }
}
