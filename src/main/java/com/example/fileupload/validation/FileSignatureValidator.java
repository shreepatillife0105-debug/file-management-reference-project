package com.example.fileupload.validation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileSignatureValidator {

    /*
     * Signature validation checks the beginning bytes of a file.
     *
     * This is intentionally separate from Apache Tika:
     * - Tika detects a MIME type.
     * - This class verifies expected signatures for our supported types.
     *
     * This list can be expanded when the project supports more formats.
     */
    private static final Map<String, byte[]> SIGNATURES = Map.of(
            "pdf", new byte[] { '%', 'P', 'D', 'F' },
            "jpg", new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF },
            "jpeg", new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF },
            "png", new byte[] {
                    (byte) 0x89, 'P', 'N', 'G',
                    0x0D, 0x0A, 0x1A, 0x0A
            }
    );

    public boolean isValid(
            MultipartFile file,
            String extension) {

        byte[] expected = SIGNATURES.get(extension);

        // If a type has no configured signature, don't silently accept it.
        if (expected == null) {
            return false;
        }

        try (InputStream inputStream = file.getInputStream()) {

            byte[] actual = inputStream.readNBytes(expected.length);

            if (actual.length != expected.length) {
                return false;
            }

            for (int i = 0; i < expected.length; i++) {
                if (actual[i] != expected[i]) {
                    return false;
                }
            }

            return true;

        } catch (IOException e) {
            return false;
        }
    }
}
