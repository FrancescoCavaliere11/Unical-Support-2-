package unical_support.unicalsupport2.service.implementation;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.data.EmailMessage;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.classifier.SingleCategoryDto;
import unical_support.unicalsupport2.data.dto.judger.JudgementResultDto;
import unical_support.unicalsupport2.data.dto.responder.ResponderResultDto;
import unical_support.unicalsupport2.service.interfaces.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrchestratorServiceImpl implements OrchestratorService {
    private final EmailReceiver emailReceiver;
    private final EmailClassifier emailClassifier;
    private final EmailSender emailSender;
    private final EmailService emailService;
    private final EmailResponder emailResponder;
    private final JudgerService judgerService;

    private final ModelMapper modelMapper;

    /**
     * Fetches emails, classifies them and forwards those labeled {@code NON_RICONOSCIUTA}.
     *
     * <p>Behavior:</p>
     * <ul>
     *   <li>Calls {@code emailReceiver.receiveEmails()} to obtain messages.</li>
     *   <li>Maps each {@code EmailMessage} to {@code ClassificationEmailDto} (subject and body).</li>
     *   <li>If no emails are present, returns immediately.</li>
     *   <li>Invokes {@code emailClassifier.classifyEmail(List)} and prints each result to stdout.</li>
     *   <li>For any result containing category {@code NON_RICONOSCIUTA} (case-insensitive),
     *       forwards the original email using {@code emailSender.sendEmail}.</li>
     *       <li>Generates email responses using {@code emailResponder.generateEmailResponse(List)}
     *       and prints them to stdout.</li>
     *       <li>Invokes {@code judgerService.judge(List, List)} to obtain judgements and prints them to stdout.</li>
     * </ul>
     *
     */
    @Override
    public void start() {
        List<EmailMessage> originalEmails = emailReceiver.receiveEmails();

        List<ClassificationEmailDto> emailsToClassify = originalEmails.stream()
                .map(emailMessage -> modelMapper.map(emailMessage, ClassificationEmailDto.class))
                .toList();

        if (emailsToClassify.isEmpty()) return;

        List<ClassificationResultDto> classificationResult = emailClassifier.classifyEmail(emailsToClassify);
        List<ResponderResultDto> responderResult = emailResponder.generateEmailResponse(classificationResult);
        List<JudgementResultDto> judgements = judgerService.judge(emailsToClassify, classificationResult);

        System.out.println("\n=== RISULTATI CLASSIFICATORE ===");

        for (int i = 0; i < classificationResult.size(); i++) {
            ClassificationResultDto r = classificationResult.get(i);
            System.out.println(r);

            List<SingleCategoryDto> categories = r.getCategories();

            boolean nonRiconosciuta = false;
            for (SingleCategoryDto c : categories) {
                if ("NON RICONOSCIUTA".equalsIgnoreCase(c.getCategory())) {
                    nonRiconosciuta = true;
                    break;
                }
            }

            if (nonRiconosciuta) {
                EmailMessage toForward = getEmailMessage(originalEmails, i);
                emailSender.sendEmail(toForward);
                emailService.saveEmailWithLowConfidence(
                    originalEmails.get(i),
                    classificationResult.get(i));
                
            }
        }

        System.out.println("\n=== RISULTATI JUDGER ===");

        for (JudgementResultDto j : judgements) {
            System.out.println(j);
            if (j.getOverallConfidence() < 0.8) {
                emailService.saveEmailWithLowConfidence(
                    originalEmails.get(j.getId()),
                    classificationResult.get(j.getId()));
            }
        }

        System.out.println("\n\n--- RISPOSTE GENERATE AUTOMATICAMENTE ---\n\n");

        for(int i = 0; i < responderResult.size(); i++) {
            ResponderResultDto r = responderResult.get(i);
            EmailMessage reviewEmail = getEmailMessageForResponder(originalEmails.get(i), r);
            emailSender.sendEmail(reviewEmail);
        }
    }

    /**
     * Builds an email to forward the original message at the given index.
     *
     * <p>The returned {@code EmailMessage} contains:
     * - a recipient,
     * - a subject prefixed with {@code "Email non riconosciuta: "},
     * - the original sender and body appended in the message body.</p>
     *
     * @param originalEmails list of received emails
     * @param i index of the email to forward
     * @return an {@code EmailMessage} ready to be sent
     */
    private static EmailMessage getEmailMessage(List<EmailMessage> originalEmails, int i) {
        EmailMessage original = originalEmails.get(i);

        EmailMessage toForward = new EmailMessage();
        toForward.setTo(List.of("lorenzo.test.04112025@gmail.com"));
        toForward.setSubject("Email non riconosciuta: " + original.getSubject());

        String sender = (original.getTo() != null && !original.getTo().isEmpty())
                ? original.getTo().getFirst()
                : "(mittente sconosciuto)";

        toForward.setBody("Mittente originale: " + sender + "\n\n" + original.getBody());
        return toForward;
    }

    /**
     * Builds an email summarizing the generated responses for review.
     *
     * <p>The returned {@code EmailMessage} contains:
     * - a recipient (hardcoded),
     * - a subject prefixed with {@code "Verifica automatica risposta per: "},
     * - the original email details and generated responses in the message body.</p>
     *
     * @param originalEmail the original email message
     * @param responderResult the generated responses to summarize
     * @return an {@code EmailMessage} ready to be sent for review
     */
    private static EmailMessage getEmailMessageForResponder(
            EmailMessage originalEmail,
            ResponderResultDto responderResult
    ) {
        EmailMessage reviewEmail = new EmailMessage();
        // Forse in questa fase potremmo semplicemente evirtare questi due campi
        reviewEmail.setInReplyToHeader(originalEmail.getInReplyToHeader());
        reviewEmail.setReferencesHeader(originalEmail.getReferencesHeader());

        reviewEmail.setTo(List.of("lorenzo.test.04112025@gmail.com"));
        reviewEmail.setSubject(originalEmail.getSubject());

        StringBuilder body = new StringBuilder();

        for (var singleResponse : responderResult.getResponses()) {
            body.append("\nCategoria: ").append(singleResponse.getCategory());
            body.append("\nTemplate: ").append(
                    singleResponse.getTemplate() != null
                            ? singleResponse.getTemplate()
                            : "(nessun template disponibile)"
            );
            body.append("\nMotivo: ").append(singleResponse.getReason());


            if (singleResponse.getParameter() != null && !singleResponse.getParameter().isEmpty()) {
                body.append("\nParametri estratti:");
                singleResponse.getParameter().forEach((k, v) ->
                        body.append("\n - ").append(k).append(": ").append(v != null ? v : "(mancante)")
                );
            }

            if (singleResponse.getContent() != null) {
                body.append("\n\nContenuto generato:\n").append(singleResponse.getContent());
            }
        }

        reviewEmail.setBody(body.toString());
        return reviewEmail;
    }
}
