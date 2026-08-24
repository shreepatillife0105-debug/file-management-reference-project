package com.example.fileupload.service;

import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /*
     * Stores the physical file and returns the generated stored filename.
     */
    String store(MultipartFile file, String extension);

    /*
     * Loads a stored file for download/view.
     */
    Resource loadAsResource(String storedFileName);

    /*
     * Deletes the physical file.
     */
    void delete(String storedFileName);

    /*
     * Returns the configured storage root.
     * Useful internally and for diagnostics.
     */
    Path getStorageLocation();
}
