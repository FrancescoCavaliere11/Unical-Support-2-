package unical_support.unicalsupport2.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unical_support.unicalsupport2.data.dto.email.EmailDto;
import unical_support.unicalsupport2.data.dto.email.UpdateAnswerDto;
import unical_support.unicalsupport2.data.dto.email.UpdateEmailCategoryDto;
import unical_support.unicalsupport2.service.interfaces.EmailService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;

    @GetMapping
    public ResponseEntity<List<EmailDto>> getEmails() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(emailService.getStoredEmail());
    }

    @PatchMapping
    public ResponseEntity<HttpStatus> updateCategory(
            @Valid @RequestBody UpdateEmailCategoryDto updateEmailCategoryDto
    ) {
        emailService.updateEmailCategory(updateEmailCategoryDto);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PutMapping("/answer")
    public ResponseEntity<EmailDto> updateAndSendEmail(
            @Valid @RequestBody UpdateAnswerDto updateAnswerDto
    ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(emailService.updateAndSendEmail(updateAnswerDto));
    }
}
