package unical_support.unicalsupport2.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.EmailMessage;
import unical_support.unicalsupport2.data.dto.classifier.SingleCategoryDto;
import unical_support.unicalsupport2.service.interfaces.EmailClassifier;
import unical_support.unicalsupport2.service.interfaces.EmailReceiver;
import unical_support.unicalsupport2.service.interfaces.EmailSender;

import java.util.List;

/**
 * Spring Shell command class to fetch, classify and forward emails.
 *
 * <p>Provides a CLI entry point to:
 * - receive messages from the configured {@code EmailReceiver},
 * - convert them to {@code ClassificationEmailDto} for classification,
 * - invoke {@code EmailClassifier} to obtain results and
 * - forward messages classified as {@code NON_RICONOSCIUTA} via {@code EmailSender}.</p>
 */
@Command(command = "start" , alias = "s", description = "Commands for start email fetching and classification")
@RequiredArgsConstructor
public class EmailCommand {

    private final EmailReceiver emailReceiver;
    private final EmailClassifier emailClassifier;
    private final EmailSender emailSender;

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
     * </ul>
     *
     * <p>Usage:</p>
     * <pre>
     * start fetch
     * s f
     * </pre>
     */
    @Command(command = "fetch", alias = "f", description = "Fetch emails from the server")
    public void fetchEmailAndClassify() {
        List<EmailMessage> originalEmails = emailReceiver.receiveEmails();

        List<ClassificationEmailDto> emailsToClassify = originalEmails.stream()
                .map(emailMessage -> {
                    ClassificationEmailDto dto = new ClassificationEmailDto();
                    dto.setSubject(emailMessage.getSubject());
                    dto.setBody(emailMessage.getBody());
                    return dto;
                })
                .toList();

        if (emailsToClassify.isEmpty()) return;


        List<ClassificationResultDto> results = emailClassifier.classifyEmail(emailsToClassify);

        for (int i = 0; i < results.size(); i++) {
            ClassificationResultDto r = results.get(i);
            System.out.println(r);

            List<SingleCategoryDto> categories = r.getCategories();

            boolean nonRiconosciuta = false;
            for (SingleCategoryDto c : categories) {
                if ("NON_RICONOSCIUTA".equalsIgnoreCase(c.getCategory())) {
                    nonRiconosciuta = true;
                    break;
                }
            }

            if (nonRiconosciuta) {
                EmailMessage toForward = getEmailMessage(originalEmails, i);
                emailSender.sendEmail(toForward);
            }
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
        toForward.setTo(List.of("misentouncavallo@gmail.com")); //TODO change email address
        toForward.setSubject("Email non riconosciuta: " + original.getSubject());

        String sender = (original.getTo() != null && !original.getTo().isEmpty())
                ? original.getTo().getFirst()
                : "(mittente sconosciuto)";

        toForward.setBody("Mittente originale: " + sender + "\n\n" + original.getBody());
        return toForward;
    }
}
