package unical_support.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailConfig {
    private final Properties props = new Properties();

    public EmailConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IOException("Impossibile trovare application.properties");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Errore nel caricamento delle configurazioni", e);
        }
    }

    public String getSmtpHost() { return props.getProperty("smtp.host"); }
    public int getSmtpPort() { return Integer.parseInt(props.getProperty("smtp.port")); }
    public String getImapHost() { return props.getProperty("imap.host"); }
    public int getImapPort() { return Integer.parseInt(props.getProperty("imap.port")); }
    public String getUsername() { return props.getProperty("mail.username"); }
    public String getPassword() { return props.getProperty("mail.password"); }
}

