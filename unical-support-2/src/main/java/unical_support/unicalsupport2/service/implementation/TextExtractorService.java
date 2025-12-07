package unical_support.unicalsupport2.service.implementation;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Service
public class TextExtractorService {
    public String extractText(File file) {
        try {
            if (file == null || !file.exists() || !file.isFile() || !file.canRead()) {
                throw new IllegalArgumentException("File non valido o non leggibile: " + (file != null ? file.getPath() : "null"));
            }

            if (file.getName().endsWith(".pdf")) {
                try (PDDocument doc = PDDocument.load(file)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    return stripper.getText(doc);
                }
            }

            if (file.getName().endsWith(".txt")) {
                return Files.readString(file.toPath());
            }

            if (file.getName().endsWith(".docx")) {
                try (XWPFDocument doc = new XWPFDocument(new FileInputStream(file))) {
                    XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
                    return extractor.getText();
                }
            }

            throw new IllegalArgumentException("Unsupported file type: " + file.getName());
        } catch (Exception e) {
            throw new RuntimeException("Error extracting text", e);
        }
    }


    public String extractText(MultipartFile file) {
        try {
            String filename = file.getOriginalFilename().toLowerCase();

            if (filename.endsWith(".pdf")) {
                try (PDDocument doc = PDDocument.load(file.getInputStream())) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    return stripper.getText(doc);
                }
            }

            if (filename.endsWith(".txt")) {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            }

            if (filename.endsWith(".docx")) {
                try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
                    XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
                    return extractor.getText();
                }
            }

            throw new IllegalArgumentException("Unsupported file type: " + filename);

        } catch (Exception e) {
            throw new RuntimeException("Error extracting text from file: " + file.getOriginalFilename(), e);
        }
    }
}

