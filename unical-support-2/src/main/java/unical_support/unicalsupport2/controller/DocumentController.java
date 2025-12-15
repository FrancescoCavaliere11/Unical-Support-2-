package unical_support.unicalsupport2.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import unical_support.unicalsupport2.data.dto.document.DocumentCreateDto;
import unical_support.unicalsupport2.data.dto.document.DocumentDto;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidMultipartExtension;
import unical_support.unicalsupport2.service.interfaces.DocumentService;

import java.util.List;


@RestController
@RequestMapping("/api/v1/document")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;

    /*
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

     */

    @PostMapping
    public ResponseEntity<DocumentDto> uploadDocument(
            @RequestParam("document")
            @NotNull
            @ValidMultipartExtension
            MultipartFile document,

            @RequestParam("documentCreateDto")
            @Valid
            DocumentCreateDto documentCreateDto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(documentService.processAndSaveDocumentFromMultipart(document, documentCreateDto));
    }

    @GetMapping
    public ResponseEntity<List<DocumentDto>> getAll() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(documentService.getAll());
    }
}
