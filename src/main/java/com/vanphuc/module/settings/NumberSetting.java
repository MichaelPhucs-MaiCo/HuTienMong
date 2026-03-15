package com.vanphuc.module.settings;

/**
 * A setting that represents a numeric value with bounds.
 */
public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;

    public NumberSetting(String name, double defaultValue, double min, double max) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
    }

    @Override
    public void setValue(Double value) {
        double clamped = Math.clamp(value, min, max);
        super.setValue(clamped);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public float getValueFloat() {
        return getValue().floatValue();
    }
}
