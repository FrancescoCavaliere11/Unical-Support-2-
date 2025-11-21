package unical_support.unicalsupport2.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unical_support.unicalsupport2.data.dto.EmailDto;
import unical_support.unicalsupport2.data.dto.UpdateEmailCategoryDto;

import java.util.List;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class EmailController {
//    private EmailService emailService;

    @GetMapping
    public ResponseEntity<List<EmailDto>> getEmail() {
        return ResponseEntity
                .status(HttpStatus.OK)
//                .body(emailService.getLowConfidenceEmail());
                .body(List.of());
    }

    @PatchMapping
    public ResponseEntity<HttpStatus> updateCategory(
            @Valid @RequestBody UpdateEmailCategoryDto updateEmailCategoryDto
    ) {
//        emailService.updateEmailCategory(updateEmailCategoryDto);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
