package unical_support.unicalsupport2.service.implementation;

import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.service.interfaces.EmailService;

@Service
public class EmailServiceImpl implements EmailService {
    @Override
    public void sendEmail(String to, String subject, String body) {

    }

    @Override
    public void receiveEmail() {

    }
}
