package com.example.fileupload.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.fileupload.dto.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileUploadException(
            FileUploadException ex) {

        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.failure(
                        ex.getMessage(),
                        "FILE_ERROR"
                ));
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileNotFoundException(
            FileNotFoundException ex) {

        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.failure(
                        ex.getMessage(),
                        "FILE_NOT_FOUND"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex) {

        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.failure(
                        "An unexpected error occurred",
                        "INTERNAL_SERVER_ERROR"
                ));
    }
}
