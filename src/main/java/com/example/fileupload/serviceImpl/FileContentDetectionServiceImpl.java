package com.example.fileupload.serviceImpl;

import java.io.IOException;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.fileupload.exception.FileUploadException;
import com.example.fileupload.service.FileContentDetectionService;

@Service
public class FileContentDetectionServiceImpl
        implements FileContentDetectionService {

    private final Tika tika;

    public FileContentDetectionServiceImpl() {
        this.tika = new Tika();
    }

    @Override
    public String detectContentType(MultipartFile file) {

        try {
            /*
             * Tika inspects the actual uploaded bytes.
             * Example:
             * invoice.pdf containing ZIP bytes can be detected as ZIP
             * instead of blindly trusting the ".pdf" extension.
             */
            return tika.detect(file.getInputStream());

        } catch (IOException e) {
            throw new FileUploadException(
                    "Unable to detect actual file content type"
            );
        }
    }
}
