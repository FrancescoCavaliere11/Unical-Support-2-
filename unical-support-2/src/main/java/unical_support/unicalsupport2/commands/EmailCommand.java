package unical_support.unicalsupport2.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.validation.annotation.Validated;
import unical_support.unicalsupport2.data.dto.ClassificationEmailDto;
import unical_support.unicalsupport2.service.interfaces.EmailClassifier;
import unical_support.unicalsupport2.service.interfaces.EmailReceiver;

import java.util.List;

@Command(command = "start" , alias = "s", description = "Commands for start email fetching and classification")
@Validated
@RequiredArgsConstructor
public class EmailCommand {
    private final EmailReceiver emailReceiver;
    private final EmailClassifier emailClassifier;

    @Command(command = "fetch", alias = "f", description = "Fetch emails from the server")
    public void fetchEmailAndClassify() {
        List<ClassificationEmailDto> emails = emailReceiver.receiveEmails()
                .stream()
                .map(emailMessage -> {
                    ClassificationEmailDto dto = new ClassificationEmailDto();
                    dto.setSubject(emailMessage.getSubject());
                    dto.setBody(emailMessage.getBody());
                    return dto;
                }).toList();

        if (!emails.isEmpty())
            emailClassifier.classifyEmail(emails).forEach(System.out::println);
    }
}