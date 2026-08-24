package com.example.fileupload.service;

import org.springframework.core.io.Resource;

import com.example.fileupload.dto.FileMetadataResponseDTO;

public interface FileDownloadService {

    Resource download(Long id);

    FileMetadataResponseDTO getMetadata(Long id);
}
