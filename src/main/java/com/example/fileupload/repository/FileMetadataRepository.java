package com.example.fileupload.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fileupload.entity.FileMetadata;
import com.example.fileupload.enums.FileStatus;

public interface FileMetadataRepository
        extends JpaRepository<FileMetadata, Long> {

    Optional<FileMetadata> findByIdAndStatus(
            Long id,
            FileStatus status
    );

    boolean existsByStoredFileName(String storedFileName);
}
