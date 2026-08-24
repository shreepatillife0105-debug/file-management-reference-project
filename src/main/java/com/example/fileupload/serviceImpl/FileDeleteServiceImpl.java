package com.example.fileupload.serviceImpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fileupload.entity.FileMetadata;
import com.example.fileupload.enums.FileStatus;
import com.example.fileupload.exception.FileNotFoundException;
import com.example.fileupload.repository.FileMetadataRepository;
import com.example.fileupload.service.FileDeleteService;
import com.example.fileupload.service.FileStorageService;

@Service
public class FileDeleteServiceImpl
        implements FileDeleteService {

    private final FileMetadataRepository fileMetadataRepository;
    private final FileStorageService fileStorageService;

    public FileDeleteServiceImpl(
            FileMetadataRepository fileMetadataRepository,
            FileStorageService fileStorageService) {

        this.fileMetadataRepository =
                fileMetadataRepository;
        this.fileStorageService =
                fileStorageService;
    }

    @Override
    @Transactional
    public void delete(Long id) {

        FileMetadata metadata =
                fileMetadataRepository
                        .findByIdAndStatus(
                                id,
                                FileStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new FileNotFoundException(
                                        "File not found with id: "
                                                + id
                                )
                        );

        /*
         * Delete physical file first.
         * If it fails, don't mark DB metadata as deleted.
         */
        fileStorageService.delete(
                metadata.getStoredFileName()
        );

        /*
         * Soft delete:
         * Keep metadata for audit/history.
         */
        metadata.setStatus(
                FileStatus.DELETED
        );

        fileMetadataRepository.save(metadata);
    }
}
