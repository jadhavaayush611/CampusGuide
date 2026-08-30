package com.campusguide.common.attachment.validation;

import com.campusguide.common.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class AttachmentValidator {

    private final long maxFileSizeBytes;

    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            ".exe", ".bat", ".cmd", ".sh", ".bash", ".js", ".vbs",
            ".jar", ".war", ".ear", ".jsp", ".php", ".py", ".bin",
            ".dll", ".so", ".dylib", ".msi", ".com", ".scr"
    );

    private static final Map<String, Set<String>> ALLOWED_MIME_TO_EXTENSIONS = Map.ofEntries(
            Map.entry("application/pdf", Set.of(".pdf")),
            Map.entry("image/jpeg", Set.of(".jpg", ".jpeg")),
            Map.entry("image/png", Set.of(".png")),
            Map.entry("image/webp", Set.of(".webp")),
            Map.entry("image/gif", Set.of(".gif")),
            Map.entry("application/msword", Set.of(".doc")),
            Map.entry("application/vnd.openxmlformats-officedocument.wordprocessingml.document", Set.of(".docx")),
            Map.entry("application/vnd.ms-excel", Set.of(".xls")),
            Map.entry("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", Set.of(".xlsx")),
            Map.entry("application/vnd.ms-powerpoint", Set.of(".ppt")),
            Map.entry("application/vnd.openxmlformats-officedocument.presentationml.presentation", Set.of(".pptx")),
            Map.entry("text/plain", Set.of(".txt", ".text", ".log")),
            Map.entry("text/csv", Set.of(".csv")),
            Map.entry("text/markdown", Set.of(".md", ".markdown")),
            Map.entry("application/zip", Set.of(".zip")),
            Map.entry("application/x-zip-compressed", Set.of(".zip"))
    );

    public AttachmentValidator(@Value("${attachments.max-file-size:20971520}") long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() == 0) {
            throw new BadRequestException("Attachment file must not be empty");
        }

        if (file.getSize() > maxFileSizeBytes) {
            long maxMb = maxFileSizeBytes / (1024 * 1024);
            throw new BadRequestException("File size (" + (file.getSize() / 1024) + " KB) exceeds maximum allowed limit of " + maxMb + " MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BadRequestException("Original filename must not be blank");
        }

        // Path traversal prevention
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new BadRequestException("Filename contains invalid or relative path characters");
        }

        String normalizedName = originalFilename.trim().toLowerCase(Locale.ROOT);
        int extIdx = normalizedName.lastIndexOf('.');
        if (extIdx == -1) {
            throw new BadRequestException("File must have a valid extension");
        }

        String extension = normalizedName.substring(extIdx);

        // Disallow dangerous extensions explicitly
        if (BLOCKED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Executable and script files are strictly prohibited");
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new BadRequestException("Content-Type header must be specified");
        }

        String normalizedContentType = contentType.toLowerCase(Locale.ROOT).split(";")[0].trim();

        // Allowed MIME validation
        if (!ALLOWED_MIME_TO_EXTENSIONS.containsKey(normalizedContentType)) {
            throw new BadRequestException("Unsupported file content type: " + normalizedContentType +
                    ". Supported types: PDF, Images (JPEG, PNG, WEBP, GIF), Documents (DOC, DOCX, XLS, XLSX, PPT, PPTX, TXT, CSV, MD), and ZIP archives.");
        }

        // Anti-spoofing: Verify extension matches claimed MIME type
        Set<String> validExtensionsForMime = ALLOWED_MIME_TO_EXTENSIONS.get(normalizedContentType);
        if (validExtensionsForMime == null || !validExtensionsForMime.contains(extension)) {
            throw new BadRequestException("File extension '" + extension + "' does not match declared Content-Type '" + normalizedContentType + "'");
        }
    }
}
