package com.example.fileupload.validation;

import java.nio.file.Paths;

public final class FileNameValidator {

    private FileNameValidator() {
    }

    /*
     * Validates only the user-supplied original filename.
     *
     * It does NOT decide whether the extension is allowed.
     * Extension/content validation belongs to FileValidationService.
     */
    public static void validate(String filename) {

        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException(
                    "File name is required"
            );
        }

        if (filename.length() > 255) {
            throw new IllegalArgumentException(
                    "File name must not exceed 255 characters"
            );
        }

        // Reject path separators and traversal attempts.
        if (filename.contains("/")
                || filename.contains("\\")
                || filename.contains("..")) {

            throw new IllegalArgumentException(
                    "Invalid file name"
            );
        }

        // Normalization check adds another layer against path-like input.
        try {
            if (!Paths.get(filename).normalize()
                    .toString()
                    .equals(filename)) {

                throw new IllegalArgumentException(
                        "Invalid file name"
                );
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid file name"
            );
        }
    }
}
