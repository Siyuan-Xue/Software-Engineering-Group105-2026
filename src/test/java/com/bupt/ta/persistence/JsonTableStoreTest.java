package com.bupt.ta.persistence;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.config.DatabaseConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonTableStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void initializeShouldCreateEmptyTableFile() {
        JsonTableStore<TestEntity> store = createStore("entities.json");

        store.initialize();

        assertTrue(Files.exists(tempDir.resolve("entities.json")));
        assertEquals(0, store.findAll().size());
    }

    @Test
    void saveShouldAssignIdAndPersistAcrossReload() {
        JsonTableStore<TestEntity> store = createStore("entities.json");
        TestEntity saved = store.save(new TestEntity(null, "resume-a", BigDecimal.ONE, null, null));

        JsonTableStore<TestEntity> reloadedStore = createStore("entities.json");
        Optional<TestEntity> loaded = reloadedStore.findById(saved.getId());

        assertTrue(loaded.isPresent());
        assertEquals("resume-a", loaded.get().getName());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void saveShouldPreserveCreatedAtAndRefreshUpdatedAt() {
        JsonTableStore<TestEntity> store = createStore("entities.json");
        TestEntity saved = store.save(new TestEntity(null, "draft", BigDecimal.ONE, null, null));

        Instant createdAt = saved.getCreatedAt();
        Instant initialUpdatedAt = saved.getUpdatedAt();
        saved.setName("updated");

        TestEntity updated = store.save(saved);

        assertEquals(createdAt, updated.getCreatedAt());
        assertNotEquals(initialUpdatedAt, updated.getUpdatedAt());
        assertEquals("updated", updated.getName());
    }

    @Test
    void deleteShouldRemoveEntity() {
        JsonTableStore<TestEntity> store = createStore("entities.json");
        TestEntity saved = store.save(new TestEntity(null, "draft", BigDecimal.ONE, null, null));

        assertTrue(store.delete(saved.getId()));
        assertTrue(store.findById(saved.getId()).isEmpty());
    }

    @Test
    void loadShouldFailFastOnMalformedJson() throws IOException {
        Path tablePath = tempDir.resolve("broken.json");
        Files.createDirectories(tempDir);
        Files.writeString(tablePath, "{ not-valid-json");
        JsonTableStore<TestEntity> store = createStore("broken.json");

        assertThrows(DataAccessException.class, store::initialize);
    }

    @Test
    void failedWriteShouldLeavePreviousVersionReadable() throws IOException {
        JsonTableStore<TestEntity> healthyStore = createStore("resilient.json");
        TestEntity saved = healthyStore.save(new TestEntity(null, "baseline", BigDecimal.ONE, null, null));

        JsonTableStore<TestEntity> failingStore = new FailingJsonTableStore(
                DatabaseConfig.of(tempDir, AppConfig.createObjectMapper()),
                "resilient.json"
        );
        saved.setName("new-value");

        assertThrows(DataAccessException.class, () -> failingStore.save(saved));

        JsonTableStore<TestEntity> reloadedStore = createStore("resilient.json");
        TestEntity reloaded = reloadedStore.findById(saved.getId()).orElseThrow();
        assertEquals("baseline", reloaded.getName());
    }

    private JsonTableStore<TestEntity> createStore(String fileName) {
        return new JsonTableStore<>(
                DatabaseConfig.of(tempDir, AppConfig.createObjectMapper()),
                fileName,
                TestEntity.class
        );
    }

    static class FailingJsonTableStore extends JsonTableStore<TestEntity> {

        FailingJsonTableStore(DatabaseConfig config, String fileName) {
            super(config, fileName, TestEntity.class);
        }

        @Override
        protected void writeEnvelope(Path path, TableEnvelope<TestEntity> envelope) throws IOException {
            if (path.getFileName().toString().endsWith(".tmp")) {
                throw new IOException("simulated write failure");
            }
            super.writeEnvelope(path, envelope);
        }
    }

    static class TestEntity implements PersistableEntity {
        private java.util.UUID id;
        private String name;
        private BigDecimal score;
        private Instant createdAt;
        private Instant updatedAt;

        public TestEntity() {
        }

        TestEntity(java.util.UUID id, String name, BigDecimal score, Instant createdAt, Instant updatedAt) {
            this.id = id;
            this.name = name;
            this.score = score;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        @Override
        public java.util.UUID getId() {
            return id;
        }

        @Override
        public void setId(java.util.UUID id) {
            this.id = id;
        }

        @Override
        public Instant getCreatedAt() {
            return createdAt;
        }

        @Override
        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }

        @Override
        public Instant getUpdatedAt() {
            return updatedAt;
        }

        @Override
        public void setUpdatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
        }

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
