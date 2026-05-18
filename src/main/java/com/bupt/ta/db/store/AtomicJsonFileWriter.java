package com.bupt.ta.db.store;

import com.bupt.ta.db.core.DatabaseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class AtomicJsonFileWriter {
    public void writeAtomically(Path target, Object payload, ObjectMapper mapper) {
        Path temp = target.resolveSibling(target.getFileName() + ".tmp");
        try {
            Files.createDirectories(target.getParent());
            try (OutputStream outputStream = Files.newOutputStream(temp)) {
                mapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, payload);
                outputStream.flush();
            }
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new DatabaseException("Failed to persist table " + target, e);
        } finally {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
                // Best effort cleanup.
            }
        }
    }
}
