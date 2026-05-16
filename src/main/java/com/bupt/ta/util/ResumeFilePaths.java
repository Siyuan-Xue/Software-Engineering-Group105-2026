package com.bupt.ta.util;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.domain.entity.Resume;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ResumeFilePaths {
    private ResumeFilePaths() {
    }

    public static Path resolve(Resume resume) {
        if (resume == null || resume.getUploadedFilePath() == null || resume.getUploadedFilePath().isBlank()) {
            return null;
        }
        String stored = resume.getUploadedFilePath().trim();
        Path direct = Path.of(stored);
        if (Files.isRegularFile(direct)) {
            return direct;
        }
        if (!direct.isAbsolute()) {
            Path underData = AppConfig.resolveDataDirectory().resolve(stored);
            if (Files.isRegularFile(underData)) {
                return underData;
            }
            Path underUploads = AppConfig.resolveDataDirectory()
                    .resolve("resumes")
                    .resolve("uploads")
                    .resolve(direct.getFileName().toString());
            if (Files.isRegularFile(underUploads)) {
                return underUploads;
            }
        }
        return null;
    }

    public static boolean isAvailable(Resume resume) {
        return resolve(resume) != null;
    }
}
