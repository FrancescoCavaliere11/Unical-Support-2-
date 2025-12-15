package unical_support.unicalsupport2.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import unical_support.unicalsupport2.data.dto.template.TemplateCreateDto;
import unical_support.unicalsupport2.data.dto.template.TemplateDto;
import unical_support.unicalsupport2.data.dto.template.TemplateUpdateDto;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidIdFormat;
import unical_support.unicalsupport2.service.interfaces.TemplateService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/template")
@RequiredArgsConstructor
public class TemplateController {
    private final TemplateService templateService;

    @GetMapping
    public ResponseEntity<List<TemplateDto>> getTemplates(
            @RequestParam(required = false) @ValidIdFormat String categoryId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(templateService.getAll(categoryId));
    }

    @PostMapping
    public ResponseEntity<TemplateDto> createTemplate(
            @Valid @RequestBody TemplateCreateDto templateCreateDto
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(templateService.createTemplate(templateCreateDto));
    }

    @PutMapping
    public ResponseEntity<TemplateDto> updateTemplate(
            @Valid @RequestBody TemplateUpdateDto templateUpdateDto
    ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(templateService.updateTemplate(templateUpdateDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteTemplate(
            @PathVariable
            @NotNull(message = "Id is required")
            @ValidIdFormat String id
    ){
        templateService.deleteTemplateById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
