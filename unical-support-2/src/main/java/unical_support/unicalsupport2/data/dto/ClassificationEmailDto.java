package unical_support.unicalsupport2.data.dto;

import lombok.Data;

@Data
public class ClassificationEmailDto {
    private String subject;
    private String body;

    @Override
    public String toString() {
        return "Subject: " + subject + "\nBody: " + body;
    }
}