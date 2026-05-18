package com.bupt.ta.db.store;

import com.bupt.ta.domain.entity.AbstractEntity;

import java.nio.file.Path;

public class TableDescriptor<T extends AbstractEntity> {
    private final String tableName;
    private final Path filePath;
    private final int version;
    private final Class<T> rowType;

    public TableDescriptor(String tableName, Path filePath, int version, Class<T> rowType) {
        this.tableName = tableName;
        this.filePath = filePath;
        this.version = version;
        this.rowType = rowType;
    }

    public String getTableName() {
        return tableName;
    }

    public Path getFilePath() {
        return filePath;
    }

    public int getVersion() {
        return version;
    }

    public Class<T> getRowType() {
        return rowType;
    }
}
