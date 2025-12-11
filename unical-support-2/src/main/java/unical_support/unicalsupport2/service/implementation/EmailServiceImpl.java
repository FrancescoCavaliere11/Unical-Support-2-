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
import unical_support.unicalsupport2.data.dto.responder.SingleResponseDto;
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
    public void saveEmail(EmailMessage emailToSave, ClassificationResultDto classificationResultDto) {

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

        newEmail.setClassifications(classifications);

        emailRepository.save(newEmail);
    }

    @Override
    @Transactional
    public void saveAnswers(ResponderResultDto responderResultDto) {
        String emailId = String.valueOf(responderResultDto.getEmailId());
        Email email = emailRepository.findById(emailId)
                .orElseThrow(() -> new RuntimeException("Email not found with ID: " + emailId));

        Answers answers = new Answers();
        answers.setEmail(email);

        boolean hasResponses = responderResultDto.getResponses() != null && !responderResultDto.getResponses().isEmpty();
        answers.setAnswered(hasResponses);

        List<SingleAnswer> singleAnswersList = new ArrayList<>();

        if (hasResponses) {
            for (SingleResponseDto responseDto : responderResultDto.getResponses()) {
                SingleAnswer singleAnswer = new SingleAnswer();

                // Se content è null, salviamo una stringa vuota ""
                String content = responseDto.getContent() != null ? responseDto.getContent() : "";
                singleAnswer.setAnswer(content);

                singleAnswer.setParameter(responseDto.getParameter());

                Category category = categoryRepository.findByNameIgnoreCase(responseDto.getCategory()) //
                        .orElseThrow(() -> new RuntimeException("Category not found: " + responseDto.getCategory()));
                singleAnswer.setCategory(category);

                Template template = templateRepository.findByNameIgnoreCase(responseDto.getTemplate()) //
                        .orElseThrow(() -> new RuntimeException("Template not found: " + responseDto.getTemplate()));
                singleAnswer.setTemplate(template);

                try {
                    double reasonVal = Double.parseDouble(responseDto.getReason());
                    singleAnswer.setReason(reasonVal);
                } catch (NumberFormatException | NullPointerException e) {
                    singleAnswer.setReason(0.0);
                }

                singleAnswersList.add(singleAnswer);
            }
        }

        answers.setSingleAnswers(singleAnswersList);

        email.setAnswers(answers);

        emailRepository.save(email);
    }
}
