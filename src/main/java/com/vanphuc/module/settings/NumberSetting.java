package com.vanphuc.module.settings;

import com.google.gson.JsonObject;

/**
 * A setting that represents a numeric value with bounds and customizable precision.
 */
public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private int precision = 1;

    public NumberSetting(String name, double defaultValue, double min, double max) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
    }

    public NumberSetting setPrecision(int precision) {
        this.precision = precision;
        return this;
    }

    public int getPrecision() {
        return precision;
    }

    @Override
    public void setValue(Double value) {
        double clamped = Math.clamp(value, min, max);
        double factor = Math.pow(10, precision);
        double rounded = Math.round(clamped * factor) / factor;
        super.setValue(rounded);
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

    @Override
    public void save(JsonObject parent) {
        parent.addProperty(getName(), getValue());
    }

    @Override
    public void load(JsonObject parent) {
        if (parent.has(getName())) {
            setValue(parent.get(getName()).getAsDouble());
        }
    }
}