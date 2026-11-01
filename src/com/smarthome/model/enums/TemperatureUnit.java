package com.smarthome.model.enums;

/** Unit used to show and enter temperatures. The domain always stores Celsius. */
public enum TemperatureUnit {
    CELSIUS("C"),
    FAHRENHEIT("F");

    private final String symbol;

    TemperatureUnit(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    /** Converts a value expressed in this unit to Celsius. */
    public double toCelsius(double value) {
        return this == CELSIUS ? value : (value - 32) * 5 / 9;
    }

    /** Converts a Celsius value to this unit, rounded to one decimal. */
    public double fromCelsius(double celsius) {
        double value = this == CELSIUS ? celsius : celsius * 9 / 5 + 32;
        return Math.round(value * 10) / 10.0;
    }
}
