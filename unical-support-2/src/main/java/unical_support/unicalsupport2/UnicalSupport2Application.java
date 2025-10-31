package unical_support.unicalsupport2;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import unical_support.unicalsupport2.EmailClassifier.LLM.Classifier;
import unical_support.unicalsupport2.EmailClassifier.LLM.ClassifierBatch;
import unical_support.unicalsupport2.EmailClassifier.Model.ClassificationResult;
import unical_support.unicalsupport2.EmailClassifier.Model.EmailData;

import java.util.List;

@SpringBootApplication
public class UnicalSupport2Application {

    public static void main(String[] args) {
        SpringApplication.run(UnicalSupport2Application.class, args);
    }
    @Bean
    CommandLineRunner demo(Classifier classifier, ClassifierBatch classifierBatch) {
        return args -> {
            System.out.println("\n=== DEMO CLASSIFICAZIONE (single) ===");
            // creo un'email di prova (usa la tua classe EmailData!)
            EmailData e1 = new EmailData(
                    "Richiesta bando Erasmus",
                    "Buongiorno, vorrei informazioni su come partecipare al bando Erasmus."
            );
            ClassificationResult r1 = classifier.classify(e1);
            System.out.println("e1 -> " + r1); // stampa: category/confidence/explanation

            System.out.println("\n=== DEMO CLASSIFICAZIONE (batch) ===");
            List<EmailData> batch = List.of(
                    new EmailData("Problemi Esse3", "Non riesco ad accedere alla piattaforma Esse3."),
                    new EmailData("Riconoscimento CFU", "Chiedo il riconoscimento degli esami svolti in Erasmus."),
                    new EmailData("Ritiro Pergamena di laurea","Salve vorrei ritirare la pergamena di laurea, a chi devo rivolgermi" )
            );
            List<ClassificationResult> out = classifierBatch.classifyBatch(batch);
            for (int i = 0; i < out.size(); i++) {
                System.out.println("ID " + i + " -> " + out.get(i));
            }

            System.out.println("\n=== FINE DEMO ===\n");
        };
    }
}
