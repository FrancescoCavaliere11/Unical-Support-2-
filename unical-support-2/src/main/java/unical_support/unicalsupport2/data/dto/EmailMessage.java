package unical_support.unicalsupport2.data.dto;


// Capire se serve mantenere in memoria temporaneamente l'email così da poter gestire l'invio
// una volta che l'email è stata classificata e una risposta ad essa geenrata.

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class EmailMessage {
    private List<String> to;
    private String subject;
    private String body;
}
