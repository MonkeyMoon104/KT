package com.monkey.kt.config.diff;

public class DiffEntry {
    private final DiffType type;
    private final String path;
    private final String fromPath;
    private final Object oldValue;
    private final Object newValue;

    private DiffEntry(DiffType type, String path, String fromPath, Object oldValue, Object newValue) {
        this.type = type;
        this.path = path;
        this.fromPath = fromPath;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public static DiffEntry of(DiffType type, String path, Object oldValue, Object newValue) {
        return new DiffEntry(type, path, null, oldValue, newValue);
    }

    public static DiffEntry moved(String fromPath, String toPath, Object value) {
        return new DiffEntry(DiffType.MOVED, toPath, fromPath, value, value);
    }

    public DiffType getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public String getFromPath() {
        return fromPath;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    @Override
    public String toString() {
        switch (type) {
            case MOVED:
                return "[MOVED] " + fromPath + " -> " + path + " = " + valueToString(newValue);
            case ADDED:
                return "[ADDED] " + path + " = " + valueToString(newValue);
            case REMOVED:
                return "[REMOVED] " + path + " (old=" + valueToString(oldValue) + ")";
            case USER_KEPT:
                return "[USER_KEPT] " + path + " old=" + valueToString(oldValue) + " new(default)=" + valueToString(newValue);
            default:
                return "[KEPT] " + path;
        }
    }

    private String valueToString(Object v) {
        return v == null ? "null" : v.toString();
    }
}
