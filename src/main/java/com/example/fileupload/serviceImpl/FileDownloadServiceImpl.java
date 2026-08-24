package com.example.fileupload.serviceImpl;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.example.fileupload.dto.FileMetadataResponseDTO;
import com.example.fileupload.entity.FileMetadata;
import com.example.fileupload.enums.FileStatus;
import com.example.fileupload.exception.FileNotFoundException;
import com.example.fileupload.mapper.FileMapper;
import com.example.fileupload.repository.FileMetadataRepository;
import com.example.fileupload.service.FileDownloadService;
import com.example.fileupload.service.FileStorageService;

@Service
public class FileDownloadServiceImpl
        implements FileDownloadService {

    private final FileMetadataRepository fileMetadataRepository;
    private final FileStorageService fileStorageService;
    private final FileMapper fileMapper;

    public FileDownloadServiceImpl(
            FileMetadataRepository fileMetadataRepository,
            FileStorageService fileStorageService,
            FileMapper fileMapper) {

        this.fileMetadataRepository =
                fileMetadataRepository;
        this.fileStorageService =
                fileStorageService;
        this.fileMapper = fileMapper;
    }

    @Override
    public Resource download(Long id) {

        FileMetadata metadata =
                findActiveFile(id);

        return fileStorageService.loadAsResource(
                metadata.getStoredFileName()
        );
    }

    @Override
    public FileMetadataResponseDTO getMetadata(Long id) {

        return fileMapper.toResponse(
                findActiveFile(id)
        );
    }

    private FileMetadata findActiveFile(Long id) {

        return fileMetadataRepository
                .findByIdAndStatus(
                        id,
                        FileStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new FileNotFoundException(
                                "File not found with id: " + id
                        )
                );
    }
}
