package com.example.fileupload.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.fileupload.config.FileProperties;
import com.example.fileupload.exception.FileUploadException;
import com.example.fileupload.service.FileContentDetectionService;
import com.example.fileupload.service.FileValidationService;
import com.example.fileupload.validation.FileNameValidator;
import com.example.fileupload.validation.FileSignatureValidator;

@Service
public class FileValidationServiceImpl
        implements FileValidationService {

    private final FileProperties fileProperties;
    private final FileSignatureValidator fileSignatureValidator;
    private final FileContentDetectionService fileContentDetectionService;

    public FileValidationServiceImpl(
            FileProperties fileProperties,
            FileSignatureValidator fileSignatureValidator,
            FileContentDetectionService fileContentDetectionService) {

        this.fileProperties = fileProperties;
        this.fileSignatureValidator = fileSignatureValidator;
        this.fileContentDetectionService = fileContentDetectionService;
    }

    @Override
    public void validateFile(MultipartFile file) {

        validateFileExists(file);
        validateNotEmpty(file);
        validateSize(file);

        String filename = file.getOriginalFilename();

        validateFilename(filename);

        String extension = getExtension(filename);

        validateExtension(extension);

        // This is the MIME type supplied by the client.
        validateContentType(file);

        // This is detected from actual file content using Apache Tika.
        String actualContentType =
                detectAndValidateActualContentType(file);

        // Extension must agree with actual content detected by Tika.
        validateExtensionMatchesContentType(
                extension,
                actualContentType
        );

        // Additional byte-signature validation.
        validateFileSignature(file, extension);
    }

    @Override
    public void validateMultipleFiles(
            List<MultipartFile> files) {

        if (files == null || files.isEmpty()) {
            throw new FileUploadException(
                    "At least one file is required"
            );
        }

        if (files.size() > fileProperties.getMaxFiles()) {
            throw new FileUploadException(
                    "Maximum "
                            + fileProperties.getMaxFiles()
                            + " files are allowed"
            );
        }

        for (MultipartFile file : files) {
            validateFile(file);
        }
    }

    private void validateFileExists(MultipartFile file) {

        if (file == null) {
            throw new FileUploadException(
                    "File is required"
            );
        }
    }

    private void validateNotEmpty(MultipartFile file) {

        if (file.isEmpty()) {
            throw new FileUploadException(
                    "File cannot be empty"
            );
        }
    }

    private void validateSize(MultipartFile file) {

        if (file.getSize() > fileProperties.getMaxSize()) {
            throw new FileUploadException(
                    "File size must not exceed "
                            + fileProperties.getMaxSize()
                            + " bytes"
            );
        }
    }

    private void validateFilename(String filename) {

        try {
            FileNameValidator.validate(filename);

        } catch (IllegalArgumentException e) {
            throw new FileUploadException(e.getMessage());
        }
    }

    private void validateExtension(String extension) {

        if (extension.isBlank()) {
            throw new FileUploadException(
                    "File extension is required"
            );
        }

        if (!fileProperties.getAllowedExtensions()
                .contains(extension)) {

            throw new FileUploadException(
                    "File extension is not allowed"
            );
        }
    }

    private void validateContentType(MultipartFile file) {

        String contentType = file.getContentType();

        if (contentType == null
                || !fileProperties.getAllowedContentTypes()
                        .contains(contentType)) {

            throw new FileUploadException(
                    "Invalid client-provided file type"
            );
        }
    }

    private String detectAndValidateActualContentType(
            MultipartFile file) {

        String actualContentType =
                fileContentDetectionService
                        .detectContentType(file);

        if (!fileProperties.getAllowedContentTypes()
                .contains(actualContentType)) {

            throw new FileUploadException(
                    "Actual file content type is not allowed: "
                            + actualContentType
            );
        }

        return actualContentType;
    }

    private void validateExtensionMatchesContentType(
            String extension,
            String actualContentType) {

        String expectedContentType =
                fileProperties.getExtensionMimeTypes()
                        .get(extension);

        if (expectedContentType == null) {
            throw new FileUploadException(
                    "No MIME mapping configured for extension: "
                            + extension
            );
        }

        if (!expectedContentType.equals(actualContentType)) {
            throw new FileUploadException(
                    "File extension does not match its actual content"
            );
        }
    }

    private void validateFileSignature(
            MultipartFile file,
            String extension) {

        boolean valid =
                fileSignatureValidator.isValid(
                        file,
                        extension
                );

        if (!valid) {
            throw new FileUploadException(
                    "File content does not match its extension"
            );
        }
    }

    private String getExtension(String filename) {

        int index = filename.lastIndexOf('.');

        if (index == -1 || index == filename.length() - 1) {
            return "";
        }

        return filename
                .substring(index + 1)
                .toLowerCase();
    }
}
