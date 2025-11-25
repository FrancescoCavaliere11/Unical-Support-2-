package unical_support.unicalsupport2.service.implementation;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import unical_support.unicalsupport2.data.dto.email.EmailToClassifyDto;
import unical_support.unicalsupport2.data.dto.email.UpdateEmailCategoryDto;
import unical_support.unicalsupport2.data.entities.Category;
import unical_support.unicalsupport2.data.entities.EmailToClassify;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.data.repositories.EmailRepository;
import unical_support.unicalsupport2.data.repositories.EmailToClassifyRepository;
import unical_support.unicalsupport2.service.interfaces.EmailService;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    
    private final EmailToClassifyRepository emailRepository;
    private final CategoryRepository categoryRepository;

    private final ModelMapper modelMapper;

    @Override
    public List<EmailToClassifyDto> getLowConfidenceEmail() {
         return emailRepository.findAllByIsClassified(false)
         .stream()
         .map(email -> modelMapper.map(email, EmailToClassifyDto.class))
         .toList();
    }

    @Override
    public void updateEmailCategory(UpdateEmailCategoryDto updateEmailCategoryDto) {

        /*EmailToClassify emailToClassify = emailRepository.findById(updateEmailCategoryDto.getId())
        .orElseThrow(RuntimeException::new); //Todo Aggiungere eccezione personalizzata

        emailToClassify.sin categoryRepository.findAllById(updateEmailCategoryDto.getCategoryIds());
        
        emailToClassify*/
    }
    
    
}
