package unical_support.unicalsupport2.service.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.responder.ResponderResultDto;
import unical_support.unicalsupport2.data.dto.responder.SingleResponseDto;
import unical_support.unicalsupport2.prompting.PromptService;
import unical_support.unicalsupport2.service.interfaces.EmailResponder;
import unical_support.unicalsupport2.service.interfaces.LlmClient;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailResponderImpl implements EmailResponder {
    private final LlmClient geminiApiClient;
    private final PromptService promptService;
    private final ObjectMapper mapper = new ObjectMapper();


    @Override
    public List<ResponderResultDto> generateEmailResponse(List<ClassificationResultDto> emails) {
        try {
            String prompt = promptService.buildResponderPrompt(emails);

            String raw = geminiApiClient.chat(prompt);

            ArrayNode arr = (ArrayNode) mapper.readTree(raw);

            List<ResponderResultDto> out = new ArrayList<>();
            for (int i = 0; i < emails.size(); i++) {
                out.add(new ResponderResultDto(i, List.of(
                        new SingleResponseDto(
                                null,
                                null,
                                null,
                                Map.of(),
                                "NO_TEMPLATE_MATCH"
                        )
                )));
            }

            for (JsonNode emailNode : arr) {
                int emailId = emailNode.path("email_id").asInt(-1);
                if (emailId < 0 || emailId >= emails.size()) continue;

                List<SingleResponseDto> responses = new ArrayList<>();

                JsonNode respArr = emailNode.path("responses");
                if (respArr.isArray()) {
                    for (JsonNode r : respArr) {

                        String category = safe(r.path("category").asText());
                        String template = r.path("template").isNull() ? null : safe(r.path("template").asText());
                        String content = r.path("content").isNull() ? null : safe(r.path("content").asText());
                        String reason = safe(r.path("reason").asText());

                        // parameters
                        Map<String,String> params = new HashMap<>();
                        JsonNode p = r.path("parameters");
                        if (p.isObject()) {
                            p.fieldNames().forEachRemaining(fieldName -> {
                                JsonNode valueNode = p.get(fieldName);
                                params.put(fieldName, valueNode.isNull() ? null : valueNode.asText());
                            });
                        }

                        responses.add(new SingleResponseDto(
                                category,
                                template,
                                content,
                                params,
                                reason
                        ));
                    }
                }

                out.set(emailId, new ResponderResultDto(emailId, responses));
            }

            return out;
        } catch (Exception x) {
            List<ResponderResultDto> fallback = new ArrayList<>();
            for (int i = 0; i < emails.size(); i++) {
                fallback.add(new ResponderResultDto(i, List.of(
                        new SingleResponseDto(
                                null, null, null, Map.of(), "NO_TEMPLATE_MATCH"
                        )
                )));
            }
            return fallback;
        }
    }


    private String safe(String s) { return s == null ? "" : s; }
}
