package unical_support.unicalsupport2.EmailClassifier.Model;


import lombok.Data;

// classe semplice per definire il formato di un'email: oggetto e contenuto

public class EmailData {
    private final String subject; // OGGETTO dell'email
    private final String body;    // CORPO dell'email

    // costruttore: se arrivano null, li trasformo in stringa vuota per evitare NPE
    public EmailData(String subject, String body) {
        this.subject = subject == null ? "" : subject;
        this.body = body == null ? "" : body;
    }

    // getter necessari per la (de)serializzazione JSON di Spring/Jackson
    public String getSubject() { return subject; }
    public String getBody() { return body; }

    @Override
    public String toString() {
        return "Subject: " + subject + "\nBody: " + body;
    }
}
