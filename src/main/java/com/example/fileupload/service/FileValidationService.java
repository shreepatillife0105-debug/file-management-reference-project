package com.example.fileupload.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface FileValidationService {

    void validateFile(MultipartFile file);

    void validateMultipleFiles(List<MultipartFile> files);
}
