package com.example.fileupload.serviceImpl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.fileupload.config.FileProperties;
import com.example.fileupload.exception.FileUploadException;
import com.example.fileupload.service.FileStorageService;

@Service
public class FileStorageServiceImpl
        implements FileStorageService {

    private final Path storageLocation;

    public FileStorageServiceImpl(
            FileProperties fileProperties) {

        try {
            /*
             * Resolve the configured directory once when the service starts.
             * The directory is created automatically if it doesn't exist.
             */
            this.storageLocation =
                    Paths.get(fileProperties.getUploadDir())
                            .toAbsolutePath()
                            .normalize();

            Files.createDirectories(this.storageLocation);

        } catch (IOException e) {
            throw new FileUploadException(
                    "Could not initialize file storage directory"
            );
        }
    }

    @Override
    public String store(
            MultipartFile file,
            String extension) {

        /*
         * UUID prevents collisions and ensures the physical filename
         * is not controlled by the user.
         */
        String storedFileName =
                UUID.randomUUID()
                        + "."
                        + extension;

        Path destination =
                storageLocation
                        .resolve(storedFileName)
                        .normalize();

        /*
         * Defense in depth:
         * The generated filename must remain inside our storage directory.
         */
        if (!destination.startsWith(storageLocation)) {
            throw new FileUploadException(
                    "Invalid storage path"
            );
        }

        try (InputStream inputStream =
                     file.getInputStream()) {

            Files.copy(
                    inputStream,
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return storedFileName;

        } catch (IOException e) {

            throw new FileUploadException(
                    "Could not store file"
            );
        }
    }

    @Override
    public Resource loadAsResource(
            String storedFileName) {

        try {

            Path filePath =
                    storageLocation
                            .resolve(storedFileName)
                            .normalize();

            if (!filePath.startsWith(storageLocation)) {
                throw new FileUploadException(
                        "Invalid file path"
                );
            }

            Resource resource =
                    new UrlResource(
                            filePath.toUri()
                    );

            if (!resource.exists()
                    || !resource.isReadable()) {

                throw new FileUploadException(
                        "Stored file does not exist"
                );
            }

            return resource;

        } catch (IOException e) {

            throw new FileUploadException(
                    "Could not load file"
            );
        }
    }

    @Override
    public void delete(String storedFileName) {

        try {

            Path filePath =
                    storageLocation
                            .resolve(storedFileName)
                            .normalize();

            if (!filePath.startsWith(storageLocation)) {
                throw new FileUploadException(
                        "Invalid file path"
                );
            }

            Files.deleteIfExists(filePath);

        } catch (IOException e) {

            throw new FileUploadException(
                    "Could not delete physical file"
            );
        }
    }

    @Override
    public Path getStorageLocation() {
        return storageLocation;
    }
}
