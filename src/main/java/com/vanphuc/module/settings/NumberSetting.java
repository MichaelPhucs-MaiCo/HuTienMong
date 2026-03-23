package com.vanphuc.module.settings;

/**
 * A setting that represents a numeric value with bounds and customizable precision.
 */
public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private int precision = 1; // Mặc định hiển thị 1 chữ số thập phân (0.1)

    public NumberSetting(String name, double defaultValue, double min, double max) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
    }

    // Hàm để Khầy set độ nhạy (ví dụ: .setPrecision(3) cho Abilities Speed)
    public NumberSetting setPrecision(int precision) {
        this.precision = precision;
        return this;
    }

    public int getPrecision() {
        return precision;
    }

    @Override
    public void setValue(Double value) {
        // Ép trong khoảng min - max
        double clamped = Math.clamp(value, min, max);

        // Làm tròn siêu mượt theo precision Khầy chọn
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
}