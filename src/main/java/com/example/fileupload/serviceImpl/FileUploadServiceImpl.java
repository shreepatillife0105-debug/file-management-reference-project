package com.example.fileupload.serviceImpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.fileupload.dto.FileMetadataResponseDTO;
import com.example.fileupload.entity.FileMetadata;
import com.example.fileupload.enums.FileStatus;
import com.example.fileupload.exception.FileUploadException;
import com.example.fileupload.mapper.FileMapper;
import com.example.fileupload.repository.FileMetadataRepository;
import com.example.fileupload.service.FileContentDetectionService;
import com.example.fileupload.service.FileStorageService;
import com.example.fileupload.service.FileUploadService;
import com.example.fileupload.service.FileValidationService;

@Service
public class FileUploadServiceImpl
        implements FileUploadService {

    private final FileValidationService fileValidationService;
    private final FileStorageService fileStorageService;
    private final FileContentDetectionService fileContentDetectionService;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileMapper fileMapper;

    public FileUploadServiceImpl(
            FileValidationService fileValidationService,
            FileStorageService fileStorageService,
            FileContentDetectionService fileContentDetectionService,
            FileMetadataRepository fileMetadataRepository,
            FileMapper fileMapper) {

        this.fileValidationService = fileValidationService;
        this.fileStorageService = fileStorageService;
        this.fileContentDetectionService =
                fileContentDetectionService;
        this.fileMetadataRepository =
                fileMetadataRepository;
        this.fileMapper = fileMapper;
    }

    @Override
    public FileMetadataResponseDTO upload(
            MultipartFile file) {

        /*
         * Validation must happen before physical storage.
         */
        fileValidationService.validateFile(file);

        String originalFileName =
                file.getOriginalFilename();

        String extension =
                getExtension(originalFileName);

        /*
         * Detect actual MIME again for metadata.
         * Validation already proved that this type is allowed.
         */
        String actualContentType =
                fileContentDetectionService
                        .detectContentType(file);

        String storedFileName =
                fileStorageService.store(
                        file,
                        extension
                );

        try {

            FileMetadata metadata =
                    new FileMetadata();

            metadata.setOriginalFileName(
                    originalFileName
            );

            metadata.setStoredFileName(
                    storedFileName
            );

            metadata.setExtension(extension);

            metadata.setContentType(
                    actualContentType
            );

            metadata.setFileSize(
                    file.getSize()
            );

            metadata.setStatus(
                    FileStatus.ACTIVE
            );

            FileMetadata saved =
                    fileMetadataRepository.save(
                            metadata
                    );

            return fileMapper.toResponse(saved);

        } catch (Exception e) {

            /*
             * If DB save fails after physical storage succeeds,
             * remove the physical file so we don't leave an orphan.
             */
            try {
                fileStorageService.delete(
                        storedFileName
                );
            } catch (Exception ignored) {
                // Log this in a production application.
            }

            throw new FileUploadException(
                    "File was stored but metadata could not be saved"
            );
        }
    }

    @Override
    public List<FileMetadataResponseDTO> uploadMultiple(
            List<MultipartFile> files) {

        /*
         * Validate ALL files before storing ANY file.
         * This prevents half-valid batches.
         */
        fileValidationService
                .validateMultipleFiles(files);

        List<FileMetadataResponseDTO> result =
                new ArrayList<>();

        for (MultipartFile file : files) {
            result.add(upload(file));
        }

        return result;
    }

    private String getExtension(String filename) {

        int index = filename.lastIndexOf('.');

        return filename
                .substring(index + 1)
                .toLowerCase();
    }
}
