package com.bupt.ta.util;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.domain.entity.Application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * File utilities for immutable resume snapshots attached to applications.
 */
public final class ApplicationSubmissionFiles {
    private ApplicationSubmissionFiles() {
    }

    public static Path submissionsDirectory() {
        return AppConfig.resolveDataDirectory().resolve("applications").resolve("submissions");
    }

    public static Path resolve(Application application) {
        if (application == null || application.getSubmittedFilePath() == null
                || application.getSubmittedFilePath().isBlank()) {
            return null;
        }
        String stored = application.getSubmittedFilePath().trim();
        Path direct = Path.of(stored);
        if (Files.isRegularFile(direct)) {
            return direct;
        }
        if (!direct.isAbsolute()) {
            Path underData = AppConfig.resolveDataDirectory().resolve(stored);
            if (Files.isRegularFile(underData)) {
                return underData;
            }
        }
        return null;
    }

    public static boolean isAvailable(Application application) {
        return resolve(application) != null;
    }

    public static void attachSnapshot(Application application, Path source, String originalFileName) throws IOException {
        if (application == null || application.getId() == null || source == null || !Files.isRegularFile(source)) {
            return;
        }
        Files.createDirectories(submissionsDirectory());
        String ext = extensionOf(originalFileName, source);
        Path destination = submissionsDirectory()
                .resolve(application.getId() + "_" + UUID.randomUUID() + ext);
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        application.setSubmittedFilePath(destination.toString());
        application.setSubmittedFileName(originalFileName != null && !originalFileName.isBlank()
                ? originalFileName
                : source.getFileName().toString());
    }

    private static String extensionOf(String originalFileName, Path source) {
        String name = originalFileName != null && originalFileName.contains(".")
                ? originalFileName
                : source.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            return name.substring(dot).toLowerCase();
        }
        return "";
    }
}
