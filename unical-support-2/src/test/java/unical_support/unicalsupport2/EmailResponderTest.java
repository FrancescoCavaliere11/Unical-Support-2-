package unical_support.unicalsupport2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.responder.ResponderResultDto;
import unical_support.unicalsupport2.data.dto.responder.SingleResponseDto;
import unical_support.unicalsupport2.prompting.PromptService;
import unical_support.unicalsupport2.service.implementation.EmailResponderImpl;
import unical_support.unicalsupport2.service.interfaces.EmailResponder;
import unical_support.unicalsupport2.service.interfaces.LlmClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailResponderTest {
    @Mock
    private LlmClient client;

    @Mock
    private PromptService promptService;

    private EmailResponder emailResponder;
    private List<ClassificationResultDto> sampleEmails;


    @BeforeEach
    void setUp() {
        emailResponder = new EmailResponderImpl(client, promptService);
        sampleEmails = List.of(
                new ClassificationResultDto(List.of(), "", 0)
        );
    }


    @Test
    void testParsingOk() throws Exception {
        String mockJson = """
            [
              {
                "email_id": 0,
                "responses": [
                  {
                    "category": "CAT",
                    "template": "T1",
                    "content": "content",
                    "parameters": { "nome": "Mario" },
                    "reason": "OK"
                  }
                ]
              }
            ]
            """;

        when(client.chat(any())).thenReturn(mockJson);

        List<ResponderResultDto> out = emailResponder.generateEmailResponse(sampleEmails);

        assertEquals(1, out.size());

        ResponderResultDto dto = out.getFirst();
        SingleResponseDto r = dto.getResponses().getFirst();

        assertEquals("CAT", r.getCategory());
        assertEquals("T1", r.getTemplate());
        assertEquals("content", r.getContent());
        assertEquals("Mario", r.getParameter().get("nome"));
        assertEquals("OK", r.getReason());
    }


    @Test
    void testNullParameters() throws Exception {
        String mockJson = """
    [
      {
        "email_id": 0,
        "responses": [
          {
            "category": "TEST",
            "template": null,
            "content": "abc",
            "parameters": { "x": null },
            "reason": "MISSING_REQUIRED_PARAMETER"
          }
        ]
      }
    ]
    """;

        when(client.chat(any())).thenReturn(mockJson);

        List<ResponderResultDto> out = emailResponder.generateEmailResponse(sampleEmails);

        assertNull(out.getFirst().getResponses().getFirst().getTemplate());
        assertNull(out.getFirst().getResponses().getFirst().getParameter().get("x"));
    }


    @Test
    void testInvalidJsonTriggersFallback() throws Exception {
        when(client.chat(any())).thenReturn("NOT JSON");

        List<ResponderResultDto> out = emailResponder.generateEmailResponse(sampleEmails);

        String reason = out.getFirst().getResponses().getFirst().getReason();


        assert(reason.startsWith("ERROR"));
    }
}
