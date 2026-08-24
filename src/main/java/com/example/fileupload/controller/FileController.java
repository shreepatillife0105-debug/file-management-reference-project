package com.example.fileupload.controller;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.fileupload.dto.ApiResponse;
import com.example.fileupload.dto.FileMetadataResponseDTO;
import com.example.fileupload.service.FileDeleteService;
import com.example.fileupload.service.FileDownloadService;
import com.example.fileupload.service.FileUploadService;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileUploadService fileUploadService;
    private final FileDownloadService fileDownloadService;
    private final FileDeleteService fileDeleteService;

    public FileController(
            FileUploadService fileUploadService,
            FileDownloadService fileDownloadService,
            FileDeleteService fileDeleteService) {

        this.fileUploadService = fileUploadService;
        this.fileDownloadService =
                fileDownloadService;
        this.fileDeleteService =
                fileDeleteService;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<
            ApiResponse<FileMetadataResponseDTO>> upload(
            @RequestParam("file")
            MultipartFile file) {

        FileMetadataResponseDTO response =
                fileUploadService.upload(file);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "File uploaded successfully"
                )
        );
    }

    @PostMapping(
            value = "/upload-multiple",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<
            ApiResponse<List<FileMetadataResponseDTO>>>
            uploadMultiple(
                    @RequestParam("files")
                    List<MultipartFile> files) {

        List<FileMetadataResponseDTO> response =
                fileUploadService.uploadMultiple(files);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Files uploaded successfully"
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FileMetadataResponseDTO>>
            metadata(@PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        fileDownloadService.getMetadata(id),
                        "File metadata retrieved successfully"
                )
        );
    }

    /*
     * Browser/view endpoint.
     * Useful for PDF/image display.
     */
    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> view(
            @PathVariable Long id) {

        FileMetadataResponseDTO metadata =
                fileDownloadService.getMetadata(id);

        Resource resource =
                fileDownloadService.download(id);

        MediaType mediaType =
                MediaType.parseMediaType(
                        metadata.getContentType()
                );

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition
                                .inline()
                                .filename(
                                        metadata.getOriginalFileName()
                                )
                                .build()
                                .toString()
                )
                .body(resource);
    }

    /*
     * Download endpoint.
     * attachment tells the browser to download the file.
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long id) {

        FileMetadataResponseDTO metadata =
                fileDownloadService.getMetadata(id);

        Resource resource =
                fileDownloadService.download(id);

        MediaType mediaType =
                MediaType.parseMediaType(
                        metadata.getContentType()
                );

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition
                                .attachment()
                                .filename(
                                        metadata.getOriginalFileName()
                                )
                                .build()
                                .toString()
                )
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id) {

        fileDeleteService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        null,
                        "File deleted successfully"
                )
        );
    }
}
