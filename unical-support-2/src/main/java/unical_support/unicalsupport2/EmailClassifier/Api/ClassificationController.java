package unical_support.unicalsupport2.EmailClassifier.Api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unical_support.unicalsupport2.EmailClassifier.LLM.Classifier;
import unical_support.unicalsupport2.EmailClassifier.LLM.ClassifierBatch;
import unical_support.unicalsupport2.EmailClassifier.Model.ClassificationResult;
import unical_support.unicalsupport2.EmailClassifier.Model.EmailData;

import java.util.List;

// Controller REST per esporre gli endpoint di classificazione
@RestController
@RequestMapping("/api/classifier")
public class ClassificationController {

    private final Classifier classifier;
    private final ClassifierBatch classifierBatch;

    public ClassificationController(Classifier classifier, ClassifierBatch classifierBatch) {
        this.classifier = classifier;
        this.classifierBatch = classifierBatch;
    }

    @PostMapping("/single")
    public ResponseEntity<ClassificationResult> classify(@RequestBody EmailData email) {
        return ResponseEntity.ok(classifier.classify(email));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ClassificationResult>> classifyBatch(@RequestBody List<EmailData> emails) {
        return ResponseEntity.ok(classifierBatch.classifyBatch(emails));
    }
}
