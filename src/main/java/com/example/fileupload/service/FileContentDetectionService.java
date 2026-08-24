package com.example.fileupload.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileContentDetectionService {

    /*
     * Detects the actual content type from file content.
     * The client-provided MultipartFile content type is not trusted as the
     * final source of truth.
     */
    String detectContentType(MultipartFile file);
}
