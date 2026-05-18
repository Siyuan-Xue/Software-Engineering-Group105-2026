package com.bupt.ta.db.store;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.db.core.DatabaseCorruptionException;
import com.bupt.ta.db.core.DatabaseException;
import com.bupt.ta.domain.entity.AbstractEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonTableStoreTest {
    @TempDir
    Path tempDir;

    @Test
    void shouldInitializeMissingTableAndPersistRows() {
        JsonTableStore<TestEntity> store = store("entities.json");

        TestEntity saved = store.save(entity("alpha"));

        assertTrue(Files.exists(tempDir.resolve("entities.json")));
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals("alpha", store.findById(saved.getId()).orElseThrow().getName());
    }

    @Test
    void queryExistsAndReplaceAllShouldWork() {
        JsonTableStore<TestEntity> store = store("entities.json");
        store.save(entity("alpha"));
        store.save(entity("beta"));

        assertTrue(store.exists(item -> "beta".equals(item.getName())));
        assertEquals(1, store.query(item -> "alpha".equals(item.getName())).size());

        store.replaceAll(List.of(entity("gamma")));

        assertEquals(1, store.list().size());
        assertEquals("gamma", store.list().get(0).getName());
        assertFalse(store.exists(item -> "alpha".equals(item.getName())));
    }

    @Test
    void malformedJsonShouldThrowDatabaseCorruptionException() throws IOException {
        Files.writeString(tempDir.resolve("broken.json"), "{not-valid-json");

        JsonTableStore<TestEntity> store = store("broken.json");

        assertThrows(DatabaseCorruptionException.class, store::list);
    }

    @Test
    void failedWriteShouldLeavePreviousVersionReadable() {
        JsonTableStore<TestEntity> healthyStore = store("resilient.json");
        TestEntity saved = healthyStore.save(entity("baseline"));
        saved.setName("updated");

        JsonTableStore<TestEntity> failingStore = new JsonTableStore<>(
                new TableDescriptor<>("resilient", tempDir.resolve("resilient.json"), 1, TestEntity.class),
                AppConfig.createObjectMapper(),
                new AtomicJsonFileWriter() {
                    @Override
                    public void writeAtomically(Path target, Object payload, com.fasterxml.jackson.databind.ObjectMapper mapper) {
                        if (target.getFileName().toString().equals("resilient.json")) {
                            throw new DatabaseException("simulated write failure");
                        }
                        super.writeAtomically(target, payload, mapper);
                    }
                }
        );

        assertThrows(DatabaseException.class, () -> failingStore.save(saved));
        assertEquals("baseline", healthyStore.findById(saved.getId()).orElseThrow().getName());
    }

    private JsonTableStore<TestEntity> store(String fileName) {
        return new JsonTableStore<>(
                new TableDescriptor<>("test", tempDir.resolve(fileName), 1, TestEntity.class),
                AppConfig.createObjectMapper(),
                new AtomicJsonFileWriter()
        );
    }

    private TestEntity entity(String name) {
        TestEntity entity = new TestEntity();
        entity.setName(name);
        entity.setScore(BigDecimal.ONE);
        return entity;
    }

    public static class TestEntity extends AbstractEntity {
        private String name;
        private BigDecimal score;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getScore() {
            return score;
        }

        public void setScore(BigDecimal score) {
            this.score = score;
        }
    }
}
