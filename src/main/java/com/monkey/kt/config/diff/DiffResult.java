package com.monkey.kt.config.diff;

import java.util.ArrayList;
import java.util.List;

public class DiffResult {
    private final List<DiffEntry> entries = new ArrayList<>();

    public void add(DiffEntry entry) {
        entries.add(entry);
    }

    public List<DiffEntry> getEntries() {
        return entries;
    }

    public boolean hasChanges() {
        return entries.stream().anyMatch(e -> e.getType() != DiffType.KEPT);
    }
}
