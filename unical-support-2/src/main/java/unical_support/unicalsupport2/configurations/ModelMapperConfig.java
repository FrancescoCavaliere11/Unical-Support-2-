package unical_support.unicalsupport2.configurations;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import unical_support.unicalsupport2.data.dto.email.SingleClassificationDto;
import unical_support.unicalsupport2.data.dto.email.UpdateSingleClassificationDto;
import unical_support.unicalsupport2.data.embeddables.SingleClassification;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.addMappings(new PropertyMap<SingleClassification, SingleClassificationDto>() {
            @Override
            protected void configure() {
                map().setCategory(source.getCategory().getId());
                map().setConfidence(source.getConfidence() * 100);
            }
        });

        modelMapper.addMappings(new PropertyMap<UpdateSingleClassificationDto, SingleClassification>() {
            @Override
            protected void configure() {
                skip().setCategory(null);
                map().setConfidence(1);
            }
        });

        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PUBLIC);

        return modelMapper;
    }
}
