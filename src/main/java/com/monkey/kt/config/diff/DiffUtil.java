package com.monkey.kt.config.diff;

import com.monkey.kt.config.util.YamlPaths;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public final class DiffUtil {

    private DiffUtil() {}

    public static DiffResult diff(FileConfiguration user, FileConfiguration defs) {
        Map<String, Object> u = YamlPaths.flatten(user);
        Map<String, Object> d = YamlPaths.flatten(defs);

        DiffResult result = new DiffResult();

        Map<String, String> valueToDefaultPath = new HashMap<>();
        d.forEach((k, v) -> valueToDefaultPath.put(stringify(v), k));

        Set<String> allPaths = new HashSet<>();
        allPaths.addAll(u.keySet());
        allPaths.addAll(d.keySet());

        for (String path : allPaths) {
            boolean inU = u.containsKey(path);
            boolean inD = d.containsKey(path);

            if (inU && !inD) {
                String candidate = valueToDefaultPath.get(stringify(u.get(path)));
                if (candidate != null && !candidate.equals(path)) {
                    result.add(DiffEntry.moved(path, candidate, u.get(path)));
                } else {
                    result.add(DiffEntry.of(DiffType.REMOVED, path, u.get(path), null));
                }
            } else if (!inU && inD) {
                result.add(DiffEntry.of(DiffType.ADDED, path, null, d.get(path)));
            } else {
                Object uv = u.get(path);
                Object dv = d.get(path);
                if (Objects.equals(stringify(uv), stringify(dv))) {
                    result.add(DiffEntry.of(DiffType.KEPT, path, uv, dv));
                } else {
                    result.add(DiffEntry.of(DiffType.USER_KEPT, path, uv, dv));
                }
            }
        }

        return result;
    }

    private static String stringify(Object o) {
        return o == null ? "null" : String.valueOf(o);
    }
}
