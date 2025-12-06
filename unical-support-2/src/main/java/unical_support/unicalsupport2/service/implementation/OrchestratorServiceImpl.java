package unical_support.unicalsupport2.service.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.data.EmailMessage;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.email.SingleEmailRequestDto;
import unical_support.unicalsupport2.data.dto.email.SingleEmailResponseDto;
import unical_support.unicalsupport2.data.dto.judger.JudgementResultDto;
import unical_support.unicalsupport2.data.dto.responder.ResponderResultDto;
import unical_support.unicalsupport2.data.dto.responder.SingleResponseDto;
import unical_support.unicalsupport2.service.interfaces.*;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrchestratorServiceImpl implements OrchestratorService {

    private final EmailReceiver emailReceiver;
    private final EmailClassifier emailClassifier;
    private final EmailSender emailSender;
    private final EmailResponder emailResponder;
    private final JudgerService judgerService;
    private final ModelMapper modelMapper;

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    @Override
    public void start(boolean sequentialMode) {
        log.info("Avvio procedura di fetch...");
        List<EmailMessage> originalEmails = emailReceiver.receiveEmails();

        if (originalEmails.isEmpty()) {
            log.info("Nessuna nuova email trovata.");
            return;
        }

        if (sequentialMode) {
            log.info("Modalità: SEQUENZIALE");
            processSequentialMode(originalEmails);
        } else {
            log.info("Modalità: BATCH");
            processBatchMode(originalEmails);
        }
    }

    private void processBatchMode(List<EmailMessage> originalEmails) {
        List<ClassificationEmailDto> emailsToClassify = originalEmails.stream()
                .map(email -> modelMapper.map(email, ClassificationEmailDto.class))
                .toList();

        List<ClassificationResultDto> classificationResult = emailClassifier.classifyEmail(emailsToClassify);
        printClassificationResults(classificationResult);

        List<JudgementResultDto> judgements = judgerService.judge(emailsToClassify, classificationResult);
        printJudgementResults(judgements);

        List<ResponderResultDto> responderResult = emailResponder.generateEmailResponse(classificationResult);

        processAndSendResponses(originalEmails, responderResult, classificationResult);
    }

    private void processSequentialMode(List<EmailMessage> originalEmails) {
        for (int i = 0; i < originalEmails.size(); i++) {
            EmailMessage currentEmail = originalEmails.get(i);
            System.out.println("\n" + BOLD + "--- ELABORAZIONE EMAIL " + (i + 1) + "/" + originalEmails.size() + " ---" + ANSI_RESET);

            ClassificationEmailDto emailDto = modelMapper.map(currentEmail, ClassificationEmailDto.class);
            List<ClassificationEmailDto> singleList = List.of(emailDto);

            List<ClassificationResultDto> classResults = emailClassifier.classifyEmail(singleList);
            printClassificationResults(classResults);

            List<JudgementResultDto> judgeResults = judgerService.judge(singleList, classResults);
            printJudgementResults(judgeResults);

            List<ResponderResultDto> respResults = emailResponder.generateEmailResponse(classResults);

            processAndSendResponses(List.of(currentEmail), respResults, classResults);
        }
    }

    private void printClassificationResults(List<ClassificationResultDto> results) {
        System.out.println("\n" + ANSI_BLUE + BOLD + "===  RISULTATI CLASSIFICATORE ===" + ANSI_RESET);
        for (ClassificationResultDto r : results) {
            System.out.println("--------------------------------------------------");
            System.out.println("ID: " + r.getId());
            r.getCategories().forEach(c ->
                    System.out.printf("  • %-30s (Conf: %.2f) -> %s%n",
                            c.getCategory(), c.getConfidence(), c.getText())
            );
            System.out.println("   Spiegazione: " + r.getExplanation());
        }
        System.out.println("--------------------------------------------------\n");
    }

    private void printJudgementResults(List<JudgementResultDto> judgements) {
        System.out.println(ANSI_YELLOW + BOLD + "===  RISULTATI JUDGER ===" + ANSI_RESET);
        for (JudgementResultDto j : judgements) {
            System.out.println("--------------------------------------------------");
            System.out.printf("ID: %d | Overall Confidence: %.2f%n", j.getId(), j.getOverallConfidence());
            j.getCategoriesEvaluation().forEach(e ->
                    System.out.printf("  • %-25s [%s] -> %s%n",
                            e.getCategory(), e.getVerdict(), e.getExplanation())
            );
            System.out.println("   Summary: " + j.getSummary());
        }
        System.out.println("--------------------------------------------------\n");
    }

    private void processAndSendResponses(List<EmailMessage> originalEmails,
                                         List<ResponderResultDto> responderResults,
                                         List<ClassificationResultDto> classifications) {

        System.out.println(ANSI_GREEN + BOLD + "===  RISPOSTE GENERATE & INVIATE ===" + ANSI_RESET);

        for (int i = 0; i < responderResults.size(); i++) {
            ResponderResultDto r = responderResults.get(i);
            EmailMessage original = originalEmails.get(i);
            ClassificationResultDto classification = classifications.get(i);

            System.out.println("--------------------------------------------------");
            System.out.printf("Email ID: %d | Destinatario: %s%n", r.getEmailId(), original.getTo());

            boolean isNonRiconosciuta = classification.getCategories().stream()
                    .anyMatch(c -> "NON RICONOSCIUTA".equalsIgnoreCase(c.getCategory()));

            if (isNonRiconosciuta) {
                System.out.println(ANSI_YELLOW + "  EMAIL NON RICONOSCIUTA -> INOLTRO ALL'OPERATORE" + ANSI_RESET);
                forwardEmailToOperator(original);
                continue;
            }

            if (r.getResponses() != null) {
                for (SingleResponseDto response : r.getResponses()) {
                    System.out.println("\n   Categoria: " + ANSI_CYAN + response.getCategory() + ANSI_RESET);

                    if ("NO_TEMPLATE_MATCH".equals(response.getReason()) || response.getContent() == null) {
                        System.out.println(ANSI_YELLOW + "      Nessuna risposta generata (No Template/RAG)" + ANSI_RESET);
                    } else {
                        System.out.println("      Template: " + response.getTemplate());
                        System.out.println("      Contenuto Generato:");
                        System.out.println(ANSI_GREEN + response.getContent() + ANSI_RESET);
                    }
                }
            }

            EmailMessage replyEmail = buildReplyEmail(original, r);
            emailSender.sendEmail(replyEmail);
            System.out.println("   Email inviata con successo.");
        }
        System.out.println("--------------------------------------------------\n");
    }

    private EmailMessage buildReplyEmail(EmailMessage original, ResponderResultDto responderResult) {
        EmailMessage reply = new EmailMessage();

        reply.setInReplyToHeader(original.getInReplyToHeader());
        reply.setReferencesHeader(original.getReferencesHeader());
        reply.setTo(original.getTo());

        String subject = original.getSubject();
        if (subject != null && !subject.toLowerCase().startsWith("re:")) {
            subject = "Re: " + subject;
        }
        reply.setSubject(subject);

        StringBuilder body = new StringBuilder();
        boolean hasContent = false;

        if (responderResult.getResponses() != null) {
            for (SingleResponseDto response : responderResult.getResponses()) {
                if (response.getContent() != null && !response.getContent().isBlank()) {
                    body.append(response.getContent()).append("\n\n");
                    hasContent = true;
                }
            }
        }

        if (!hasContent) {
            body.append("Gentile utente,\n\n")
                    .append("Abbiamo ricevuto la tua richiesta ma non siamo riusciti a elaborare una risposta automatica specifica.\n")
                    .append("La tua pratica è stata inoltrata a un operatore che ti risponderà al più presto.\n\n")
                    .append("Cordiali saluti,\nSegreteria Studenti");
        }

        reply.setBody(body.toString());
        return reply;
    }

    private void forwardEmailToOperator(EmailMessage original) {
        EmailMessage toForward = new EmailMessage();
        toForward.setTo(List.of("lorenzo.test.04112025@gmail.com"));
        toForward.setSubject(" [NON RICONOSCIUTA] Fwd: " + original.getSubject());

        String originalSender = (original.getTo() != null && !original.getTo().isEmpty())
                ? original.getTo().getFirst()
                : "(sconosciuto)";

        toForward.setBody(
                "Attenzione: Il sistema non ha saputo classificare questa email.\n\n" +
                        "--- Messaggio Originale ---\n" +
                        "Mittente: " + originalSender + "\n" +
                        "Testo:\n" + original.getBody()
        );

        emailSender.sendEmail(toForward);
    }



    @Override
    public SingleEmailResponseDto processSingleEmail(SingleEmailRequestDto request) {
        ClassificationEmailDto emailDto = new ClassificationEmailDto();
        emailDto.setSubject(request.getSubject());
        emailDto.setBody(request.getBody());
        List<ClassificationEmailDto> batch = List.of(emailDto);

        var classificationResults = emailClassifier.classifyEmail(batch);
        var singleClassification = classificationResults.get(0);
        var judgementResults = judgerService.judge(batch, classificationResults);
        var singleJudgement = judgementResults.get(0);
        var responderResults = emailResponder.generateEmailResponse(classificationResults);
        var singleResponse = responderResults.get(0);

        return SingleEmailResponseDto.builder()
                .classification(singleClassification.getCategories())
                .judgerConfidence(singleJudgement.getOverallConfidence())
                .judgerVerdict(singleJudgement.getSummary())
                .generatedResponses(singleResponse.getResponses())
                .build();
    }
}