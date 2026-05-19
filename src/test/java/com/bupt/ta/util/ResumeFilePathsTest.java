package com.bupt.ta.util;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.domain.entity.Resume;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResumeFilePathsTest {
    @TempDir
    Path tempDir;

    @Test
    void resolveShouldHandleAbsoluteDataRelativeAndUploadsFallbacks() throws Exception {
        Path absolute = tempDir.resolve("absolute.pdf");
        Files.writeString(absolute, "absolute");
        Resume resume = new Resume();
        resume.setUploadedFilePath(absolute.toString());
        assertEquals(absolute, ResumeFilePaths.resolve(resume));

        System.setProperty(AppConfig.DATA_DIR_PROPERTY, tempDir.toString());
        try {
            Path relative = tempDir.resolve("stored/resume.txt");
            Files.createDirectories(relative.getParent());
            Files.writeString(relative, "relative");
            resume.setUploadedFilePath("stored/resume.txt");
            assertEquals(relative, ResumeFilePaths.resolve(resume));

            Path upload = tempDir.resolve("resumes/uploads/fallback.pdf");
            Files.createDirectories(upload.getParent());
            Files.writeString(upload, "upload");
            resume.setUploadedFilePath("fallback.pdf");
            assertEquals(upload, ResumeFilePaths.resolve(resume));
            assertTrue(ResumeFilePaths.isAvailable(resume));
        } finally {
            System.clearProperty(AppConfig.DATA_DIR_PROPERTY);
        }
    }

    @Test
    void resolveShouldReturnNullForMissingOrBlankPaths() {
        assertNull(ResumeFilePaths.resolve(null));
        Resume resume = new Resume();
        resume.setUploadedFilePath(" ");
        assertNull(ResumeFilePaths.resolve(resume));
        assertFalse(ResumeFilePaths.isAvailable(resume));
    }
}
