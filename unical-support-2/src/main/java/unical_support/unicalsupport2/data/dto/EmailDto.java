package unical_support.unicalsupport2.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class EmailDto {
    private String id;
    private List<String> from;
    private String subject;
    private String content;
    private Double confidence;
    private CategoryDto categoryDto;
}
