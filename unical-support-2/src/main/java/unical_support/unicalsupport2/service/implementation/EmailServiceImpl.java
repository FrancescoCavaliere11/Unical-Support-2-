package unical_support.unicalsupport2.service.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import unical_support.unicalsupport2.data.EmailMessage;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.email.*;
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
    private final GmailSenderImpl gmailSender;

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

                     AnswerDto answerDto = modelMapper.map(email.getAnswers(), AnswerDto.class);
                     emailDto.setAnswer(answerDto);

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

                            String content = sr.getContent() != null ? sr.getContent() : "";
                            singleAnswer.setAnswer(content);

                            if(sr.getTemplate() != null && !sr.getTemplate().equals("NO_TEMPLATE_MATCH")) {
                                Template template = templateRepository.findByNameIgnoreCase(sr.getTemplate()) //
                                        .orElseThrow(() -> new RuntimeException("Template not found: " + sr.getTemplate()));
                                singleAnswer.setTemplate(template);
                            }

                            return singleAnswer;
                        }).toList()
        );

        answers.setEmail(email);
        email.setAnswers(answers);
        emailRepository.save(email);
    }

    @Transactional
    @Override
    public EmailDto updateAndSendEmail(UpdateAnswerDto updateAnswerDto) {
        Email email = emailRepository.findByAnswers_Id(updateAnswerDto.getId())
                .orElseThrow(() -> new RuntimeException("Email not found for Answers id: " + updateAnswerDto.getId()));

        if(email.getAnswers().getAnswered())
            throw new RuntimeException("Answers already provided for Answers id: " + updateAnswerDto.getId());

        List<SingleAnswer> updatedSingleAnswers = updateAnswerDto.getSingleAnswers()
                .stream()
                .map(dto -> {
                    SingleAnswer singleAnswer = modelMapper.map(dto, SingleAnswer.class);

                    if(dto.getTemplateId() != null){
                        Template template = templateRepository.findById(dto.getTemplateId())
                                .orElseThrow(() -> new RuntimeException("Template not found: " + dto.getTemplateId()));

                        singleAnswer.setTemplate(template);
                    }

                    return singleAnswer;
                })
                .toList();

        email.getAnswers().setSingleAnswers(updatedSingleAnswers);
        email.getAnswers().setAnswered(true);


        Email updatedEmail = emailRepository.save(email);
        gmailSender.sendEmail(updatedEmail);

        EmailDto emailDto = modelMapper.map(updatedEmail, EmailDto.class);
        System.out.println("Updated Email mapped to EmailDto.");
        return emailDto;
    }

}
