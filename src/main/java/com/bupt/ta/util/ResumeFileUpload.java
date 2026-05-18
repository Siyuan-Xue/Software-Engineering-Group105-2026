package com.bupt.ta.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

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
        Files.createDirectories(uploadDirectory);
        String ext = extensionOf(originalName);
        String savedName = userId + "_" + UUID.randomUUID() + ext;
        Path uploadPath = uploadDirectory.resolve(savedName);
        try (InputStream in = content) {
            Files.copy(in, uploadPath, StandardCopyOption.REPLACE_EXISTING);
        }
        return new SavedResumeFile(uploadPath, originalName);
    }

    public record SavedResumeFile(Path path, String originalFileName) {
    }
}
