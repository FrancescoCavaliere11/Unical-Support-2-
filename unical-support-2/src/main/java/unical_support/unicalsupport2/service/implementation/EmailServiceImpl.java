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
import unical_support.unicalsupport2.data.embeddables.SingleClassification;
import unical_support.unicalsupport2.data.entities.Category;
import unical_support.unicalsupport2.data.entities.Classifications;
import unical_support.unicalsupport2.data.entities.Email;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.data.repositories.ClassificationsRepository;
import unical_support.unicalsupport2.data.repositories.EmailRepository;
import unical_support.unicalsupport2.service.interfaces.EmailService;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    
    private final EmailRepository emailRepository;
    private final ClassificationsRepository classificationsRepository;
    private final CategoryRepository categoryRepository;

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
}
