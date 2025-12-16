package unical_support.unicalsupport2.configurations;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import unical_support.unicalsupport2.data.dto.document.DocumentCreateDto;
import unical_support.unicalsupport2.data.dto.email.EmailDto;
import unical_support.unicalsupport2.data.dto.email.UpdateSingleAnswerDto;
import unical_support.unicalsupport2.data.dto.email.UpdateSingleClassificationDto;
import unical_support.unicalsupport2.data.dto.template.TemplateCreateDto;
import unical_support.unicalsupport2.data.embeddables.SingleAnswer;
import unical_support.unicalsupport2.data.embeddables.SingleClassification;
import unical_support.unicalsupport2.data.entities.Document;
import unical_support.unicalsupport2.data.entities.Email;
import unical_support.unicalsupport2.data.entities.Template;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        Converter<DocumentCreateDto, LocalDateTime> nowConverter =
                ctx -> LocalDateTime.now();

        modelMapper.addMappings(new PropertyMap<TemplateCreateDto, Template>() {

            @Override
            protected void configure() {
                skip().setId(null);
            }
        });

        modelMapper.addMappings(new PropertyMap<Email, EmailDto>() {
            @Override
            protected void configure() {
                skip().setClassify(null); // Per poter impostare la confidence
                skip().setAnswer(null); // Per poter impostare le singleAnswers
            }
        });

        modelMapper.addMappings(new PropertyMap<UpdateSingleClassificationDto, SingleClassification>() {
            @Override
            protected void configure() {
                skip().setCategory(null);
                map().setConfidence(1);
            }
        });

        // mapping da UpdateSingleAnswerDto a SingleAnswer
        modelMapper.addMappings(new PropertyMap<UpdateSingleAnswerDto, SingleAnswer>() {
            @Override
            protected void configure() {
                skip().setTemplate(null);
            }
        });

        // mapping da DocumentCreateDto a Document
        modelMapper.addMappings(new PropertyMap<DocumentCreateDto, Document>() {
            @Override
            protected void configure() {
                skip().setId(null);
                skip().setOriginalFilename(null);
                skip().setChunks(null);
                skip().setCategory(null);
                skip().setFileType(null);


                using(nowConverter).map(source, destination.getCreateAt());
            }
        });

        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PUBLIC);

        return modelMapper;
    }
}
