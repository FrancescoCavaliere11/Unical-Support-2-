    package unical_support.unicalsupport2.data.dto.email;

    import java.util.List;

    import jakarta.validation.Valid;
    import jakarta.validation.constraints.NotNull;
    import lombok.Data;
    import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidIdFormat;

    @Data
    public class UpdateEmailCategoryDto {
        @NotNull(message = "Id is required")
        @ValidIdFormat
        private String id;

        @Valid
        private List<UpdateSingleClassificationDto> updateSingleClassificationDtos;
    }
