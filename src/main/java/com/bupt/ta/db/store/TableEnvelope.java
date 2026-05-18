package com.bupt.ta.db.store;

import java.util.ArrayList;
import java.util.List;

public class TableEnvelope<T> {
    private int version = 1;
    private List<T> rows = new ArrayList<>();

    public TableEnvelope() {
    }

    public TableEnvelope(int version, List<T> rows) {
        this.version = version;
        this.rows = rows;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }
}
