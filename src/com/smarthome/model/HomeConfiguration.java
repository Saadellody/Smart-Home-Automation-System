package com.smarthome.model;

import com.smarthome.model.enums.TemperatureUnit;

public class HomeConfiguration {

    private TemperatureUnit temperatureUnit = TemperatureUnit.CELSIUS;
    private double defaultTemperature = 21;
    private boolean securityEnabled = false;
    private boolean notificationsEnabled = true;
    private boolean autoModeEnabled = false;

    public TemperatureUnit getTemperatureUnit() {
        return temperatureUnit;
    }

    public void setTemperatureUnit(TemperatureUnit temperatureUnit) {
        if (temperatureUnit == null) {
            throw new IllegalArgumentException("Temperature unit is required");
        }
        this.temperatureUnit = temperatureUnit;
    }

    /** Converts a temperature entered in the configured unit to Celsius. */
    public double toCelsius(double value) {
        return temperatureUnit.toCelsius(value);
    }

    /** Converts a Celsius temperature to the configured unit, for display. */
    public double fromCelsius(double celsius) {
        return temperatureUnit.fromCelsius(celsius);
    }

    /** Default thermostat temperature, in Celsius. Applied to newly created thermostats. */
    public double getDefaultTemperature() {
        return defaultTemperature;
    }

    public void setDefaultTemperature(double defaultTemperature) {
        if (defaultTemperature < Thermostat.MIN_TEMPERATURE || defaultTemperature > Thermostat.MAX_TEMPERATURE) {
            throw new IllegalArgumentException("Default temperature must be between "
                    + Thermostat.MIN_TEMPERATURE + " and " + Thermostat.MAX_TEMPERATURE + " C");
        }
        this.defaultTemperature = defaultTemperature;
    }

    public boolean isSecurityEnabled() {
        return securityEnabled;
    }

    /** Use {@link SmartHome#armSecurity()} / {@link SmartHome#disarmSecurity()} so devices follow the flag. */
    void setSecurityEnabled(boolean securityEnabled) {
        this.securityEnabled = securityEnabled;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public boolean isAutoModeEnabled() {
        return autoModeEnabled;
    }

    public void setAutoModeEnabled(boolean autoModeEnabled) {
        this.autoModeEnabled = autoModeEnabled;
    }
}
