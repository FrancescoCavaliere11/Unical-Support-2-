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

@RequiredArgsConstructor
@Command(command = "fetch", alias = "f", description = "Fetch emails from the server")
public class EmailCommand {

    private final EmailReceiver emailReceiver;
    private final EmailClassifier emailClassifier;
    //Ho aggiunto l'eamil sender per inviare direttamente alla mail box
    //Le email classificare come NON_RICONOSCIUTE
    private final EmailSender emailSender;   

    public void fetchEmailAndClassify() {
        
        //Leggo le email che arrivano al sistema
        List<EmailMessage> originalEmails = emailReceiver.receiveEmails();

        //Le meail originali vengono trasformate in EmailDaClassificae
        List<ClassificationEmailDto> emailsToClassify = originalEmails.stream()
                .map(emailMessage -> {
                    ClassificationEmailDto dto = new ClassificationEmailDto();
                    dto.setSubject(emailMessage.getSubject());
                    dto.setBody(emailMessage.getBody());
                    return dto;
                }).toList();
        //Esce in caso non ci sono email da classificare
        if (emailsToClassify.isEmpty()) {
            return;
        }

        //Fase di classificazione della mail
        List<ClassificationResultDto> results = emailClassifier.classifyEmail(emailsToClassify);

        //Stampa da togliere
        //results.forEach(System.out::println);

        //Qui si riconoscono le email con la Category = "NON_RICONOSCIUTA"
        for (int i = 0; i < results.size(); i++) {
            ClassificationResultDto r = results.get(i);
            if ("NON_RICONOSCIUTA".equalsIgnoreCase(r.getCategory())) {   // usa il tuo getter/record
                EmailMessage original = originalEmails.get(i);

                EmailMessage toForward = new EmailMessage();
                //Mettere indirizzo della mail box del centro residenziale
                toForward.setTo(List.of("unical-scarti@tuodominio.it"));
                toForward.setSubject("NON_RICONOSCIUTA" + " " + original.getSubject());

                String sender = (original.getTo() != null && !original.getTo().isEmpty())
                        ? original.getTo().get(0)
                        : "(mittente sconosciuto)";
                
                toForward.setBody("Mittente originale: " + sender + "\n\n" + original.getBody());
                //Invio della mail alla nuova mail box
                emailSender.sendEmail(toForward);
            }
        }
    }
}