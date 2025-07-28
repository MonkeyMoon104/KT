package com.monkey.kt.config;

import java.util.Arrays;

public final class Version {

    private Version() {}

    public static int compare(String a, String b) {
        int[] av = parse(a);
        int[] bv = parse(b);
        for (int i = 0; i < Math.max(av.length, bv.length); i++) {
            int ai = i < av.length ? av[i] : 0;
            int bi = i < bv.length ? bv[i] : 0;
            if (ai != bi) return Integer.compare(ai, bi);
        }
        return 0;
    }

    private static int[] parse(String v) {
        return Arrays.stream(v.replaceAll("[^0-9.]", "").split("\\."))
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .toArray();
    }
}
