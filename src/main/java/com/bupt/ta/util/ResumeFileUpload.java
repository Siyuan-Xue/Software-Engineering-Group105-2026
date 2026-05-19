package com.bupt.ta.util;

import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

/**
 * Handles safe storage of TA resume uploads.
 */
public final class ResumeFileUpload {
    private static final String ALLOWED_EXT = "\\.(pdf|doc|docx|jpg|jpeg|png|txt)$";

    private ResumeFileUpload() {
    }

    public static boolean isAllowedFileName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return false;
        }
        String ext = extensionOf(originalName);
        return ext.matches(ALLOWED_EXT);
    }

    public static String extensionOf(String originalName) {
        String base = Paths.get(originalName).getFileName().toString();
        return base.contains(".") ? base.substring(base.lastIndexOf('.')).toLowerCase() : "";
    }

    public static SavedResumeFile save(Path uploadDirectory, UUID userId, String originalName, InputStream content)
            throws IOException {
        return save(uploadDirectory, userId, originalName, null, content);
    }

    public static SavedResumeFile save(Path uploadDirectory, UUID userId, String originalName,
                                       String contentType, InputStream content) throws IOException {
        Files.createDirectories(uploadDirectory);
        String ext = extensionOf(originalName);
        if (!isAllowedFileName(originalName)) {
            throw new IOException("Unsupported resume file type.");
        }
        String savedName = userId + "_" + UUID.randomUUID() + ext;
        Path uploadPath = uploadDirectory.resolve(savedName);
        try (BufferedInputStream in = new BufferedInputStream(content)) {
            in.mark(1024);
            byte[] header = in.readNBytes(1024);
            in.reset();
            if (!isAllowedContent(ext, contentType, header)) {
                throw new IOException("Unsupported or mismatched resume file content.");
            }
            Files.copy(in, uploadPath, StandardCopyOption.REPLACE_EXISTING);
        }
        return new SavedResumeFile(uploadPath, originalName);
    }

    public static boolean isAllowedContent(String extension, String contentType, byte[] header) {
        String ext = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
        String type = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        if (!isAllowedMime(ext, type)) {
            return false;
        }
        byte[] h = header == null ? new byte[0] : header;
        return switch (ext) {
            case ".pdf" -> startsWith(h, "%PDF-".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            case ".png" -> startsWith(h, new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a});
            case ".jpg", ".jpeg" -> h.length >= 3
                    && (h[0] & 0xff) == 0xff
                    && (h[1] & 0xff) == 0xd8
                    && (h[2] & 0xff) == 0xff;
            case ".doc" -> startsWith(h, new byte[]{(byte) 0xd0, (byte) 0xcf, 0x11, (byte) 0xe0,
                    (byte) 0xa1, (byte) 0xb1, 0x1a, (byte) 0xe1});
            case ".docx" -> startsWith(h, new byte[]{0x50, 0x4b, 0x03, 0x04})
                    || startsWith(h, new byte[]{0x50, 0x4b, 0x05, 0x06})
                    || startsWith(h, new byte[]{0x50, 0x4b, 0x07, 0x08});
            case ".txt" -> looksLikePlainText(h);
            default -> false;
        };
    }

    private static boolean isAllowedMime(String ext, String type) {
        if (type == null || type.isBlank() || "application/octet-stream".equals(type)) {
            return true;
        }
        return switch (ext) {
            case ".pdf" -> "application/pdf".equals(type);
            case ".png" -> "image/png".equals(type);
            case ".jpg", ".jpeg" -> "image/jpeg".equals(type) || "image/pjpeg".equals(type);
            case ".txt" -> type.startsWith("text/plain");
            case ".doc" -> "application/msword".equals(type);
            case ".docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(type)
                    || "application/zip".equals(type);
            default -> false;
        };
    }

    private static boolean startsWith(byte[] value, byte[] prefix) {
        if (value.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (value[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean looksLikePlainText(byte[] header) {
        for (byte b : header) {
            int value = b & 0xff;
            if (value == 0) {
                return false;
            }
            if (value < 0x09) {
                return false;
            }
        }
        return true;
    }

    /**
     * Stored upload path and original client-provided filename.
     */
    public record SavedResumeFile(Path path, String originalFileName) {
    }
}
