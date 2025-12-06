package unical_support.unicalsupport2.service.implementation;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import unical_support.unicalsupport2.data.EmailMessage;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.email.EmailToClassifyDto;
import unical_support.unicalsupport2.data.dto.email.UpdateEmailCategoryDto;
import unical_support.unicalsupport2.data.embeddables.SingleClassification;
import unical_support.unicalsupport2.data.entities.Category;
import unical_support.unicalsupport2.data.entities.EmailToClassify;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.data.repositories.EmailToClassifyRepository;
import unical_support.unicalsupport2.service.interfaces.EmailService;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    
    private final EmailToClassifyRepository emailRepository;
    private final CategoryRepository categoryRepository;

    private final ModelMapper modelMapper;

    @Override
     public List<EmailToClassifyDto> getStoredEmail(Boolean isClassified) {
         return emailRepository.findAllByIsClassified(isClassified)
         .stream()
         .map(email -> {
             EmailToClassifyDto dto = modelMapper.map(email, EmailToClassifyDto.class);
             dto.getSingleClassifications()
                     .forEach(singleClassificationDto ->
                             singleClassificationDto.setConfidence(singleClassificationDto.getConfidence() * 100));
             return dto;
         })
         .toList();
    }

    @Override
    public void updateEmailCategory(UpdateEmailCategoryDto updateEmailCategoryDto) {
        EmailToClassify emailToClassify = emailRepository.findById(updateEmailCategoryDto.getId())
                //Todo Aggiungere eccezione personalizzata
                .orElseThrow(() -> new RuntimeException("EmailToClassify not found: " + updateEmailCategoryDto.getId()));



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

        emailToClassify.setSingleClassifications(new ArrayList<>(newClassifications));
        emailToClassify.setClassified(true);

        emailRepository.save(emailToClassify);
    }

    //Così quando nell' orchestrator usiamo il metodo diamo la possibilità di specificare se è classificata o no
    @Override
    public void saveEmail(EmailMessage emailToSave, boolean classified, ClassificationResultDto classificationResultDto) {

        EmailToClassify emailToClassify = modelMapper.map(emailToSave, EmailToClassify.class);
        emailToClassify.setClassified(classified);
        emailToClassify.setExplanation(classificationResultDto.getExplanation());
        
        emailToClassify.setSingleClassifications(
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
        emailRepository.save(emailToClassify);
    }
}
