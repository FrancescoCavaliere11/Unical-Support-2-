package unical_support.unicalsupport2.service.interfaces;

import unical_support.unicalsupport2.data.dto.EmailMessage;

import java.util.List;

public interface EmailReceiver {
    List<EmailMessage> receiveEmails();
}
