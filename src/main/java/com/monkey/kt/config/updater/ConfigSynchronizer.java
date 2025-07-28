package com.monkey.kt.config.updater;

import com.monkey.kt.config.Version;
import com.monkey.kt.config.diff.*;
import com.monkey.kt.config.util.YamlMergeUtil;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Logger;

public class ConfigSynchronizer {

    private final Logger logger;
    private final String versionKey;

    public ConfigSynchronizer(Logger logger, String versionKey) {
        this.logger = logger;
        this.versionKey = versionKey;
    }

    public String sync(FileConfiguration user, FileConfiguration defs) {
        String defVersion = defs.getString(versionKey, "0.0.0");
        String userVersion = user.getString(versionKey, "0.0.0");

        DiffResult diff = DiffUtil.diff(user, defs);

        if (Version.compare(userVersion, defVersion) < 0) {
            user.set(versionKey, defVersion);
        }

        String mergedYaml = YamlMergeUtil.mergeWithOrderWithSpacing(user, defs, versionKey);

        printReport(diff);
        return mergedYaml;
    }


    private void printReport(DiffResult diff) {
        logger.info("==== Config Update Report ====");
        diff.getEntries().stream()
                .sorted((a, b) -> a.getType().compareTo(b.getType()))
                .forEach(e -> logger.info(e.toString()));
        logger.info("==== End Report ====");
    }
}
