package com.example.fileupload.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.fileupload.dto.FileMetadataResponseDTO;

public interface FileUploadService {

    FileMetadataResponseDTO upload(
            MultipartFile file
    );

    List<FileMetadataResponseDTO> uploadMultiple(
            List<MultipartFile> files
    );
}
