package unical_support.unicalsupport2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unical_support.unicalsupport2.data.dto.UpdateEmailCategoryDto;

import java.util.List;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class EmailController {
//    private EmailService emailService;

    @GetMapping
    public ResponseEntity<List<String>> getEmail() {
        return ResponseEntity
                .status(HttpStatus.OK)
//                .body(emailService.getLowConfidenceEmail());
                .body(List.of("Email 1", "Email 2"));
    }

    @PatchMapping
    public ResponseEntity<List<HttpStatus>> updateCategory(UpdateEmailCategoryDto updateEmailCategoryDto) {
//        emailService.updateEmailCategory(updateEmailCategoryDto);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
