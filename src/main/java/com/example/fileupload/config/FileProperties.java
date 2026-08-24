package com.example.fileupload.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*
 * Central configuration for all file-related rules.
 *
 * Why this class exists:
 * - Avoid hardcoding file limits and allowed types in services.
 * - Makes the application easy to change for another project.
 * - Keeps validation/business code clean.
 *
 * application.properties uses the prefix: file
 */
@Component
@ConfigurationProperties(prefix = "file")
public class FileProperties {

    private String uploadDir;
    private long maxSize;
    private int maxFiles;
    private List<String> allowedExtensions;
    private List<String> allowedContentTypes;
    private Map<String, String> extensionMimeTypes;

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public int getMaxFiles() {
        return maxFiles;
    }

    public void setMaxFiles(int maxFiles) {
        this.maxFiles = maxFiles;
    }

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public List<String> getAllowedContentTypes() {
        return allowedContentTypes;
    }

    public void setAllowedContentTypes(List<String> allowedContentTypes) {
        this.allowedContentTypes = allowedContentTypes;
    }

    public Map<String, String> getExtensionMimeTypes() {
        return extensionMimeTypes;
    }

    public void setExtensionMimeTypes(Map<String, String> extensionMimeTypes) {
        this.extensionMimeTypes = extensionMimeTypes;
    }
}
