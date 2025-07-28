package com.monkey.kt.config.backup;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.logging.Logger;
import java.util.stream.Stream;

public final class BackupUtil {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private BackupUtil() {}

    public static void backup(Logger logger, File original, int retain) {
        if (!original.exists()) return;
        File parent = original.getParentFile();
        String base = original.getName();
        String ts = LocalDateTime.now().format(TS);
        File backup = new File(parent, base + ".backup-" + ts);

        try {
            Files.copy(original.toPath(), backup.toPath());
            logger.info("Created config backup: " + backup.getName());
            prune(logger, parent, base, retain);
        } catch (IOException e) {
            logger.warning("Impossible create backup of " + base + ": " + e.getMessage());
        }
    }

    private static void prune(Logger logger, File folder, String base, int retain) {
        try (Stream<File> stream = Files.list(folder.toPath())
                .map(java.nio.file.Path::toFile)
                .filter(f -> f.getName().startsWith(base + ".backup-"))
                .sorted(Comparator.comparing(File::getName).reversed())) {

            File[] backups = stream.toArray(File[]::new);
            for (int i = retain; i < backups.length; i++) {
                if (backups[i].delete()) {
                    logger.info("Removed old backup: " + backups[i].getName());
                }
            }
        } catch (IOException ignored) {}
    }
}
