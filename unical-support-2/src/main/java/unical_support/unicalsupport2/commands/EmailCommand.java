package unical_support.unicalsupport2.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.validation.annotation.Validated;
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
    private final EmailSender emailSender;

    public void fetchEmailAndClassify() {
        //Prendo le mail originali
        List<EmailMessage> originalEmails = emailReceiver.receiveEmails();

        //Preparo le mail per classificarle
        List<ClassificationEmailDto> emailsToClassify = originalEmails.stream()
                .map(emailMessage -> {
                    ClassificationEmailDto dto = new ClassificationEmailDto();
                    dto.setSubject(emailMessage.getSubject());
                    dto.setBody(emailMessage.getBody());
                    return dto;
                })
                .toList();

        //Se non ci sono mail ca classificare esco
        if (emailsToClassify.isEmpty()) return;
        

        //Uso il classificatore per classificare la varie mail
        List<ClassificationResultDto> results = emailClassifier.classifyEmail(emailsToClassify);

    
        //results.forEach(System.out::println);

        //Uso il for per vedere quali mail non sono state riconosciute
        for (int i = 0; i < results.size(); i++) {
            ClassificationResultDto r = results.get(i);
            if ("NON_RICONOSCIUTA".equalsIgnoreCase(r.getCategory())) {
                //Recupero la mail originale, per trovare il subject e il body orginale
                EmailMessage original = originalEmails.get(i);

                //Creazione della nuova mail che dovremo spedire
                EmailMessage toForward = new EmailMessage();
                //Qui viene specificato a chi verrà inoltrata la mail
                toForward.setTo(List.of("unical-scarti@tuodominio.it"));
                //Inserisco l'oggetto della mial che devo inoltrare
                toForward.setSubject("NON_RICONOSCIUTA" + " " + original.getSubject());

                //Recupero il mittente, se non esiste inserisco una frase di fallback
                String sender = (original.getTo() != null && !original.getTo().isEmpty())
                        ? original.getTo().get(0)
                        : "(mittente sconosciuto)";
                
                        //Costruisco la mail
                toForward.setBody("Mittente originale: " + sender + "\n\n" + original.getBody());

                //Chiamo send email per inoltrarle alla mailbox che gestirà le mail non riconosciute
                emailSender.sendEmail(toForward);
            }
        }
    }
}
