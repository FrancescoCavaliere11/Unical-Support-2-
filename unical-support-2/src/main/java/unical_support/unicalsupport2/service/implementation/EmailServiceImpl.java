package unical_support.unicalsupport2.service.implementation;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import unical_support.unicalsupport2.data.EmailMessage;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.email.ClassifyDto;
import unical_support.unicalsupport2.data.dto.email.EmailDto;
import unical_support.unicalsupport2.data.dto.email.UpdateEmailCategoryDto;
import unical_support.unicalsupport2.data.dto.responder.ResponderResultDto;
import unical_support.unicalsupport2.data.embeddables.SingleAnswer;
import unical_support.unicalsupport2.data.embeddables.SingleClassification;
import unical_support.unicalsupport2.data.entities.*;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.data.repositories.ClassificationsRepository;
import unical_support.unicalsupport2.data.repositories.EmailRepository;
import unical_support.unicalsupport2.data.repositories.TemplateRepository;
import unical_support.unicalsupport2.service.interfaces.EmailService;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    
    private final EmailRepository emailRepository;
    private final ClassificationsRepository classificationsRepository;
    private final CategoryRepository categoryRepository;
    private final TemplateRepository templateRepository;

    private final ModelMapper modelMapper;

    @Override
     public List<EmailDto> getStoredEmail() {
         return emailRepository.findAll()
                 .stream()
                 .map(email -> {
                     //TODO verificare cjhe il mapping avvenga correttamente
                     EmailDto emailDto = modelMapper.map(email, EmailDto.class);

                     ClassifyDto classifyDto = modelMapper.map(email.getClassifications(), ClassifyDto.class);
                     classifyDto.getSingleClassifications()
                             .forEach(singleClassificationDto ->
                                     singleClassificationDto.setConfidence(singleClassificationDto.getConfidence() * 100));

                     emailDto.setClassify(classifyDto);

                     return emailDto;
                 })
                 .toList();
    }

    @Override
    public void updateEmailCategory(UpdateEmailCategoryDto updateEmailCategoryDto) {
        Classifications classifications = classificationsRepository.findById(updateEmailCategoryDto.getId())
                //Todo Aggiungere eccezione personalizzata
                .orElseThrow(() -> new RuntimeException("Classifications not found: " + updateEmailCategoryDto.getId()));



        List<SingleClassification> newClassifications = updateEmailCategoryDto.getUpdateSingleClassificationDtos().
                stream()
                .map(dto -> {
                    SingleClassification classification = modelMapper.map(dto, SingleClassification.class);

                    Category category = categoryRepository.findById(dto.getCategoryId())
                            .orElseThrow(() -> new RuntimeException("Category not found: " + dto.getCategoryId()));

                    classification.setCategory(category);

                    return classification;
                })
                .toList();

        classifications.setSingleClassifications(new ArrayList<>(newClassifications));
        classifications.setClassified(true);

        classificationsRepository.save(classifications);
    }

    @Override
    @Transactional
    public Email saveEmail(EmailMessage emailToSave, ClassificationResultDto classificationResultDto) {

        Email newEmail = modelMapper.map(emailToSave, Email.class);

        Classifications classifications = new Classifications();
        classifications.setClassified(false);
        classifications.setExplanation(classificationResultDto.getExplanation());

        classifications.setSingleClassifications(
            classificationResultDto.getCategories()
                .stream()
                .map(sc -> {
                    SingleClassification singleClassification = modelMapper.map(sc, SingleClassification.class);
                    Category category = categoryRepository.findByNameIgnoreCase(sc.getCategory())
                            .orElseThrow(() -> new RuntimeException("Category not found: " + sc.getCategory()));
                    singleClassification.setCategory(category);

                    return singleClassification;
                }).toList()
        );

        classifications.setEmail(newEmail);
        newEmail.setClassifications(classifications);

        return emailRepository.save(newEmail);
    }

    @Override
    @Transactional
    public void saveAnswers(Email email, ResponderResultDto responderResultDto) {
        Answers answers = new Answers();
        answers.setAnswered(false);

        answers.setSingleAnswers(
                responderResultDto.getResponses()
                        .stream()
                        .map(sr -> {
                            SingleAnswer singleAnswer = new SingleAnswer();

                            // Se content è null, salviamo una stringa vuota ""
                            String content = sr.getContent() != null ? sr.getContent() : "";
                            singleAnswer.setAnswer(content);

                            singleAnswer.setParameter(sr.getParameter());

                            Category category = sr.getCategory() == null
                                    ? categoryRepository.findByNameIgnoreCase("NON RICONOSCIUTA")
                                        .orElseThrow(() -> new RuntimeException("Category not found: NON RICONOSCIUTA"))
                                    : categoryRepository.findByNameIgnoreCase(sr.getCategory())
                                        .orElseThrow(() -> new RuntimeException("Category not found: " + sr.getCategory()));
                            singleAnswer.setCategory(category);

                            if(sr.getTemplate() != null) {
                                Template template = templateRepository.findByNameIgnoreCase(sr.getTemplate()) //
                                        .orElseThrow(() -> new RuntimeException("Template not found: " + sr.getTemplate()));
                                singleAnswer.setTemplate(template);
                            }

                            try {
                                double reasonVal = Double.parseDouble(sr.getReason());
                                singleAnswer.setReason(reasonVal);
                            } catch (NumberFormatException | NullPointerException e) {
                                singleAnswer.setReason(0.0);
                            }

                            return singleAnswer;
                        }).toList()
        );

        answers.setEmail(email);
        email.setAnswers(answers);
        emailRepository.save(email);
    }

}
