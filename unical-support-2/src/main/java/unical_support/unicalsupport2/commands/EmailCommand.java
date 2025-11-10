package unical_support.unicalsupport2.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import unical_support.unicalsupport2.data.dto.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.EmailMessage;
import unical_support.unicalsupport2.service.interfaces.EmailClassifier;
import unical_support.unicalsupport2.service.interfaces.EmailReceiver;
import unical_support.unicalsupport2.service.interfaces.EmailSender;

import java.util.List;

@Command(command = "start" , alias = "s", description = "Commands for start email fetching and classification")
@RequiredArgsConstructor
public class EmailCommand {

    private final EmailReceiver emailReceiver;
    private final EmailClassifier emailClassifier;
    private final EmailSender emailSender;

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
            if ("NON_RICONOSCIUTA".equalsIgnoreCase(r.getCategory())) {
                EmailMessage toForward = getEmailMessage(originalEmails, i);
                emailSender.sendEmail(toForward);
            }
        }
    }

    private static EmailMessage getEmailMessage(List<EmailMessage> originalEmails, int i) {
        EmailMessage original = originalEmails.get(i);

        EmailMessage toForward = new EmailMessage();
        //TODO change email address
        toForward.setTo(List.of("misentouncavallo@gmail.com"));
        toForward.setSubject("Email non riconosciuta: " + original.getSubject());

        String sender = (original.getTo() != null && !original.getTo().isEmpty())
                ? original.getTo().getFirst()
                : "(mittente sconosciuto)";

        toForward.setBody("Mittente originale: " + sender + "\n\n" + original.getBody());
        return toForward;
    }
}
