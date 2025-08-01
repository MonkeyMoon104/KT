package com.monkey.kt.utils.damage;

public class DamageConfig {
    private final boolean enabled;
    private final double value;
    private final double radius;

    public DamageConfig(boolean enabled, double value, double radius) {
        this.enabled = enabled;
        this.value = value;
        this.radius = radius;
    }

    public boolean isEnabled() { return enabled; }
    public double getValue() { return value; }
    public double getRadius() { return radius; }
}

