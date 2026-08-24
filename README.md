# Spring Boot File Management Reference Project

A complete future-reference project for learning and implementing file upload,
download, viewing, deletion, validation, Apache Tika content detection,
signature validation, local filesystem storage, and database metadata.

## Technology

- Java 17
- Spring Boot 3.3.6
- Spring Web
- Spring Data JPA
- Hibernate
- MySQL
- Apache Tika
- Maven

---

# 1. Project Goal

This project is intentionally modular.

Each responsibility has its own service/interface where appropriate.

Example:

```text
Controller
   ↓
Upload Service
   ↓
Validation Service
   ↓
Storage Service
   ↓
Repository
```

This makes the design reusable in future Spring Boot projects.

---

# 2. Package Structure

```text
com.example.fileupload
│
├── config
│   └── FileProperties
│
├── controller
│   └── FileController
│
├── dto
│   ├── ApiResponse
│   └── FileMetadataResponseDTO
│
├── entity
│   └── FileMetadata
│
├── enums
│   └── FileStatus
│
├── exception
│   ├── FileUploadException
│   ├── FileNotFoundException
│   └── GlobalExceptionHandler
│
├── mapper
│   └── FileMapper
│
├── repository
│   └── FileMetadataRepository
│
├── service
│   ├── FileUploadService
│   ├── FileDownloadService
│   ├── FileDeleteService
│   ├── FileStorageService
│   ├── FileValidationService
│   └── FileContentDetectionService
│
├── serviceImpl
│   ├── FileUploadServiceImpl
│   ├── FileDownloadServiceImpl
│   ├── FileDeleteServiceImpl
│   ├── FileStorageServiceImpl
│   ├── FileValidationServiceImpl
│   └── FileContentDetectionServiceImpl
│
└── validation
    ├── FileNameValidator
    └── FileSignatureValidator
```

---

# 3. Important Design Rule

Do not put all file functionality into one service.

For example:

```text
FileUploadService
    → upload business flow

FileValidationService
    → validation

FileStorageService
    → physical file operations

FileDownloadService
    → download/view

FileDeleteService
    → delete

FileContentDetectionService
    → Apache Tika

FileSignatureValidator
    → magic/signature validation
```

This is easier to maintain and easier to replace later.

---

# 4. File Storage Design

The actual file is stored on the filesystem.

The database stores metadata.

```text
MySQL
│
└── file_metadata
    ├── id
    ├── original_file_name
    ├── stored_file_name
    ├── extension
    ├── content_type
    ├── file_size
    ├── status
    ├── created_at
    └── updated_at

Filesystem
│
└── uploads/
    ├── UUID.pdf
    ├── UUID.jpg
    └── UUID.png
```

Do not store the user's original filename as the physical filename.

---

# 5. Why UUID Storage Names?

User uploads:

```text
invoice.pdf
```

We store:

```text
550e8400-e29b-41d4-a716-446655440000.pdf
```

Benefits:

- Prevents filename collisions.
- Does not trust user-controlled names.
- Avoids path manipulation.
- Makes physical storage predictable.

---

# 6. Validation Pipeline

```text
MultipartFile
     ↓
file exists?
     ↓
empty?
     ↓
size?
     ↓
filename?
     ↓
extension?
     ↓
client MIME?
     ↓
Apache Tika actual MIME?
     ↓
extension ↔ actual MIME?
     ↓
file signature?
     ↓
PASS
```

---

# 7. Why Client MIME Is Not Enough

`MultipartFile.getContentType()` is information supplied by the request/client.

It should not be treated as the final truth.

Example:

```text
invoice.pdf
Client MIME: application/pdf
Actual bytes: ZIP
```

Tika can detect the actual content as a different type.

---

# 8. Apache Tika

Tika is used here to detect the actual file content type.

Example:

```text
PDF      → application/pdf
JPEG     → image/jpeg
PNG      → image/png
ZIP      → application/zip
```

This helps detect files that have been renamed with a misleading extension.

---

# 9. Signature / Magic Bytes

The project additionally checks signatures for supported formats.

Examples:

```text
PDF  → %PDF
JPEG → FF D8 FF
PNG  → 89 50 4E 47 0D 0A 1A 0A
```

Tika and signature validation have separate responsibilities.

---

# 10. Upload API

## Single Upload

```http
POST /api/files/upload
Content-Type: multipart/form-data
```

Form-data:

```text
file = invoice.pdf
```

Success:

```json
{
  "success": true,
  "message": "File uploaded successfully",
  "data": {
    "id": 1,
    "originalFileName": "invoice.pdf",
    "storedFileName": "UUID.pdf",
    "extension": "pdf",
    "contentType": "application/pdf",
    "fileSize": 12345,
    "status": "ACTIVE",
    "createdAt": "...",
    "updatedAt": "..."
  },
  "error": null,
  "meta": null
}
```

---

# 11. Multiple Upload

```http
POST /api/files/upload-multiple
Content-Type: multipart/form-data
```

Form-data:

```text
files = invoice.pdf
files = photo.jpg
files = image.png
```

Maximum is configured by:

```properties
file.max-files=5
```

All files are validated before the batch starts storing files.

---

# 12. Metadata API

```http
GET /api/files/{id}
```

Example:

```http
GET /api/files/1
```

Returns metadata using `ApiResponse`.

---

# 13. View API

```http
GET /api/files/{id}/view
```

Uses:

```text
Content-Disposition: inline
```

Useful for:

- PDF browser preview
- Image browser preview

---

# 14. Download API

```http
GET /api/files/{id}/download
```

Uses:

```text
Content-Disposition: attachment
```

The original filename is returned to the browser as the download name.

---

# 15. Delete API

```http
DELETE /api/files/{id}
```

The project:

1. Finds active metadata.
2. Deletes the physical file.
3. Changes database status to `DELETED`.

The metadata is retained for audit/history.

---

# 16. Soft Delete

Instead of immediately deleting the database row:

```text
ACTIVE → DELETED
```

This preserves metadata history.

The physical file is removed.

---

# 17. Path Traversal Protection

Never resolve a user-provided path directly.

Bad:

```java
Paths.get(userInput)
```

The project uses:

```text
storage root
    +
controlled UUID filename
```

and checks:

```java
destination.startsWith(storageLocation)
```

This protects the storage boundary.

---

# 18. Filename Validation

The project rejects:

```text
../file.pdf
..\file.pdf
/path/file.pdf
\path\file.pdf
```

The original filename is metadata only.

---

# 19. File Size Protection

Application-level limit:

```properties
file.max-size=10485760
```

Spring multipart limit:

```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB
```

Both are useful:

- Spring protects the HTTP multipart request.
- Application validation protects business rules.

---

# 20. Important Difference

A valid file does not automatically mean a safe file.

For example:

```text
Valid PDF
```

does not guarantee:

```text
No malicious content
```

For higher-security systems consider antivirus/malware scanning.

---

# 21. Future Production Improvements

This reference project uses local filesystem storage.

For production environments, consider:

- AWS S3
- Azure Blob Storage
- Google Cloud Storage
- Object storage
- Antivirus scanning
- Authentication/authorization
- Per-user access control
- Encryption
- Signed download URLs
- File retention policies
- Background virus scanning
- Large-file streaming
- Chunked/resumable uploads
- CDN
- Audit logs

The important architecture benefit is that storage is behind:

```text
FileStorageService
```

so the implementation can later be replaced without changing upload business logic.

---

# 22. Database vs Filesystem

Do not confuse:

```text
File metadata
```

with:

```text
File binary data
```

This project uses:

```text
Database → metadata
Filesystem → binary
```

For large files this is generally more practical than putting the entire file into a database BLOB.

---

# 23. Common Mistakes

### Mistake 1

Trusting only:

```java
file.getOriginalFilename()
```

### Mistake 2

Trusting only:

```java
file.getContentType()
```

### Mistake 3

Storing before validation.

### Mistake 4

Using original filename as physical storage filename.

### Mistake 5

Building filesystem paths from arbitrary user input.

### Mistake 6

Putting upload/download/delete/storage/validation into one giant service.

### Mistake 7

Returning the database entity directly from the controller.

### Mistake 8

Saving a database record without ensuring the physical file was stored.

### Mistake 9

Ignoring orphan files when database storage fails.

### Mistake 10

Assuming Tika is an antivirus scanner.

---

# 24. Complete Flow

## Upload

```text
POST /api/files/upload
        ↓
Controller
        ↓
FileUploadService
        ↓
FileValidationService
        ├── filename
        ├── extension
        ├── client MIME
        ├── Tika
        └── signature
        ↓
FileStorageService
        ↓
UUID filename
        ↓
Filesystem
        ↓
FileMetadataRepository
        ↓
MySQL
        ↓
ApiResponse
```

## Download

```text
GET /api/files/1/download
        ↓
Controller
        ↓
FileDownloadService
        ↓
Repository
        ↓
FileStorageService
        ↓
Resource
        ↓
HTTP response
```

## Delete

```text
DELETE /api/files/1
        ↓
FileDeleteService
        ↓
Repository
        ↓
FileStorageService.delete()
        ↓
Physical file removed
        ↓
DB status = DELETED
```

---

# 25. How to Run

Create MySQL database:

```sql
CREATE DATABASE file_management;
```

Update:

```properties
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

Then run:

```bash
mvn clean install
mvn spring-boot:run
```

The `uploads` directory is created automatically.

---

# 26. Recommended Learning Order

Use this project in this order:

```text
1. MultipartFile
2. Configuration
3. ApiResponse
4. Validation
5. Tika
6. Signature validation
7. Storage
8. Upload
9. Database metadata
10. Download
11. View
12. Delete
13. Multiple upload
14. Security
15. Testing
16. Production storage
```

---

# 27. One Important Limitation of This Reference Project

The current project intentionally keeps storage local so the concepts are easy to understand.

Later, the same:

```java
FileStorageService
```

can have another implementation:

```text
LocalFileStorageServiceImpl
S3FileStorageServiceImpl
AzureBlobStorageServiceImpl
```

without rewriting:

```text
FileUploadService
FileDownloadService
FileValidationService
```

That is one of the main reasons we separated the services.
