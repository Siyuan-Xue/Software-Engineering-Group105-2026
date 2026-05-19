package com.bupt.ta.support;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.db.core.JsonStoreConfig;
import com.bupt.ta.db.facade.FileTaDatabase;
import com.bupt.ta.db.facade.TaDatabase;

import java.nio.file.Path;

public final class TestDatabases {
    private TestDatabases() {
    }

    public static TaDatabase open(Path dataDir) {
        return FileTaDatabase.open(JsonStoreConfig.of(dataDir, AppConfig.createObjectMapper()));
    }
}
