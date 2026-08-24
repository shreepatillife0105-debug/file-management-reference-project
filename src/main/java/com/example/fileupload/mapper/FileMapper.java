package com.example.fileupload.mapper;

import org.springframework.stereotype.Component;

import com.example.fileupload.dto.FileMetadataResponseDTO;
import com.example.fileupload.entity.FileMetadata;

@Component
public class FileMapper {

    /*
     * Mapper keeps entity-to-DTO conversion outside services.
     */
    public FileMetadataResponseDTO toResponse(
            FileMetadata entity) {

        FileMetadataResponseDTO dto =
                new FileMetadataResponseDTO();

        dto.setId(entity.getId());
        dto.setOriginalFileName(
                entity.getOriginalFileName()
        );
        dto.setStoredFileName(
                entity.getStoredFileName()
        );
        dto.setExtension(
                entity.getExtension()
        );
        dto.setContentType(
                entity.getContentType()
        );
        dto.setFileSize(
                entity.getFileSize()
        );
        dto.setStatus(
                entity.getStatus()
        );
        dto.setCreatedAt(
                entity.getCreatedAt()
        );
        dto.setUpdatedAt(
                entity.getUpdatedAt()
        );

        return dto;
    }
}
