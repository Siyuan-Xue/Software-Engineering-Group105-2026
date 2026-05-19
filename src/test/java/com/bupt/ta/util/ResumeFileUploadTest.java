package com.bupt.ta.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResumeFileUploadTest {

    @TempDir
    Path tempDir;

    @Test
    void saveShouldAcceptMatchingPdfMagicAndMime() throws Exception {
        byte[] content = "%PDF-1.7\nbody".getBytes(StandardCharsets.US_ASCII);

        ResumeFileUpload.SavedResumeFile saved = ResumeFileUpload.save(
                tempDir,
                UUID.randomUUID(),
                "resume.pdf",
                "application/pdf",
                new ByteArrayInputStream(content));

        assertTrue(java.nio.file.Files.exists(saved.path()));
    }

    @Test
    void saveShouldRejectExtensionSpoofing() {
        byte[] content = "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8);

        assertThrows(IOException.class, () -> ResumeFileUpload.save(
                tempDir,
                UUID.randomUUID(),
                "resume.pdf",
                "application/pdf",
                new ByteArrayInputStream(content)));
    }

    @Test
    void contentCheckShouldRejectBinaryTextFile() {
        assertFalse(ResumeFileUpload.isAllowedContent(".txt", "text/plain", new byte[]{'h', 'i', 0, 'x'}));
        assertTrue(ResumeFileUpload.isAllowedContent(".txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)));
    }
}
