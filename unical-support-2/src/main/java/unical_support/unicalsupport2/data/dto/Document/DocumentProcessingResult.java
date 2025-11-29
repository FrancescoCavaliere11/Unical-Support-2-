    package unical_support.unicalsupport2.data.dto.Document;

    import lombok.AllArgsConstructor;
    import lombok.Data;

    @Data
    @AllArgsConstructor
    public class DocumentProcessingResult {
        private String documentId;
        private String originalFilename;
        private String fileType;
        private String categoryName;
        private int chunksCount;
    }
