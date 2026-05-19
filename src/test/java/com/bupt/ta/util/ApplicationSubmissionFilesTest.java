package com.bupt.ta.util;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.domain.entity.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationSubmissionFilesTest {
    @TempDir
    Path tempDir;

    @Test
    void attachSnapshotShouldCopyFileAndStoreOriginalName() throws Exception {
        System.setProperty(AppConfig.DATA_DIR_PROPERTY, tempDir.toString());
        try {
            Path source = tempDir.resolve("source.DOCX");
            Files.writeString(source, "resume content");
            Application application = new Application();
            application.setId(UUID.randomUUID());

            ApplicationSubmissionFiles.attachSnapshot(application, source, "Original.DOCX");

            assertEquals("Original.DOCX", application.getSubmittedFileName());
            assertTrue(application.getSubmittedFilePath().endsWith(".docx"));
            assertTrue(ApplicationSubmissionFiles.isAvailable(application));
            assertEquals("resume content", Files.readString(ApplicationSubmissionFiles.resolve(application)));
        } finally {
            System.clearProperty(AppConfig.DATA_DIR_PROPERTY);
        }
    }

    @Test
    void attachSnapshotShouldIgnoreInvalidInputsAndResolveRelativeDataPath() throws Exception {
        System.setProperty(AppConfig.DATA_DIR_PROPERTY, tempDir.toString());
        try {
            Application missingId = new Application();
            ApplicationSubmissionFiles.attachSnapshot(missingId, tempDir.resolve("missing.pdf"), null);
            assertNull(missingId.getSubmittedFilePath());

            Path relative = tempDir.resolve("applications/submissions/stored.pdf");
            Files.createDirectories(relative.getParent());
            Files.writeString(relative, "stored");
            Application application = new Application();
            application.setSubmittedFilePath("applications/submissions/stored.pdf");

            assertEquals(relative, ApplicationSubmissionFiles.resolve(application));
            assertTrue(ApplicationSubmissionFiles.isAvailable(application));
        } finally {
            System.clearProperty(AppConfig.DATA_DIR_PROPERTY);
        }
    }

    @Test
    void resolveShouldReturnNullForMissingPath() {
        Application application = new Application();
        application.setSubmittedFilePath("missing.pdf");

        assertNull(ApplicationSubmissionFiles.resolve(application));
        assertFalse(ApplicationSubmissionFiles.isAvailable(application));
    }
}
