package unical_support.unicalsupport2.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidCategoryId;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidMultipartExtension;
import unical_support.unicalsupport2.service.interfaces.DocumentService;


@RestController
@RequestMapping("/api/v1/document")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<HttpStatus> uploadDocument(
            @RequestParam("document")
            @NotNull
            @ValidMultipartExtension
            MultipartFile document,

            @RequestParam("categoryId")
            @ValidCategoryId
            @NotBlank
            String categoryId
    ) {
        documentService.processAndSaveDocumentFromMultipart(document, categoryId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}
