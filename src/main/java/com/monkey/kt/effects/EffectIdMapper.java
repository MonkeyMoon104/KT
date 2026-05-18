package com.monkey.kt.effects;

import java.util.*;

public class EffectIdMapper {

    private final Map<String, String> aliasToCanonical = new HashMap<>();
    private final Map<String, String> canonicalToConfigKey = new HashMap<>();
    private final Map<String, Set<String>> canonicalToAcceptedIds = new HashMap<>();

    public void clear() {
        aliasToCanonical.clear();
        canonicalToConfigKey.clear();
        canonicalToAcceptedIds.clear();
    }

    public void registerBuiltIn(String configKey, String canonicalId, Collection<String> aliases) {
        String normalizedConfigKey = normalize(configKey);
        String normalizedCanonicalId = normalize(canonicalId);
        if (normalizedConfigKey.isEmpty() || normalizedCanonicalId.isEmpty()) {
            return;
        }

        canonicalToConfigKey.put(normalizedCanonicalId, normalizedConfigKey);
        registerAlias(normalizedCanonicalId, normalizedCanonicalId);
        registerAlias(normalizedConfigKey, normalizedCanonicalId);

        if (aliases != null) {
            for (String alias : aliases) {
                registerAlias(alias, normalizedCanonicalId);
            }
        }
    }

    public void registerCustom(String canonicalId, Collection<String> aliases) {
        String normalizedCanonicalId = normalize(canonicalId);
        if (normalizedCanonicalId.isEmpty()) {
            return;
        }

        registerAlias(normalizedCanonicalId, normalizedCanonicalId);
        if (aliases != null) {
            for (String alias : aliases) {
                registerAlias(alias, normalizedCanonicalId);
            }
        }
    }

    public String resolve(String effectId) {
        String normalized = normalize(effectId);
        if (normalized.isEmpty()) {
            return normalized;
        }
        return aliasToCanonical.getOrDefault(normalized, normalized);
    }

    public String getConfigKey(String effectId) {
        String canonical = resolve(effectId);
        return canonicalToConfigKey.getOrDefault(canonical, canonical);
    }

    public Set<String> getAcceptedIds(String effectId) {
        String canonical = resolve(effectId);
        if (canonical.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> accepted = canonicalToAcceptedIds.get(canonical);
        if (accepted == null || accepted.isEmpty()) {
            return Collections.singleton(canonical);
        }
        return new LinkedHashSet<>(accepted);
    }

    private void registerAlias(String alias, String canonicalId) {
        String normalizedAlias = normalize(alias);
        if (normalizedAlias.isEmpty()) {
            return;
        }

        aliasToCanonical.put(normalizedAlias, canonicalId);
        canonicalToAcceptedIds
                .computeIfAbsent(canonicalId, key -> new LinkedHashSet<>())
                .add(normalizedAlias);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
