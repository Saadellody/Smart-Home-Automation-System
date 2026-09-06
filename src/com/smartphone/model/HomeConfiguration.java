package com.smartphone.model;

public class HomeConfiguration {

    private HomeConfiguration homeConfiguration;
    private double defaultTemperature;
    private boolean securityEnabled;
    private boolean notificationsEnabled;
    private boolean autoModeEnabled;

    public HomeConfiguration(HomeConfiguration homeConfiguration, double defaultTemperature, boolean securityEnabled, boolean notificationsEnabled, boolean autoModeEnabled) {
        this.homeConfiguration = homeConfiguration;
        this.defaultTemperature = defaultTemperature;
        this.securityEnabled = securityEnabled;
        this.notificationsEnabled = notificationsEnabled;
        this.autoModeEnabled = autoModeEnabled;
    }

    public HomeConfiguration getHomeConfiguration() {
        return homeConfiguration;
    }

    public void setHomeConfiguration(HomeConfiguration homeConfiguration) {
        this.homeConfiguration = homeConfiguration;
    }

    public double getDefaultTemperature() {
        return defaultTemperature;
    }

    public void setDefaultTemperature(double defaultTemperature) {
        this.defaultTemperature = defaultTemperature;
    }

    public boolean isSecurityEnabled() {
        return securityEnabled;
    }

    public void setSecurityEnabled(boolean securityEnabled) {
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
//    Attributes
//
//    I'd start with:
//
//    HomeConfiguration
//├── temperatureUnit : TemperatureUnit
//├── defaultTemperature : double
//├── securityEnabled : boolean
//├── notificationsEnabled : boolean
//└── autoModeEnabled : boolean
//            Explanation
//    Attribute	Type	Purpose
//    temperatureUnit	TemperatureUnit	Celsius/Fahrenheit
//    defaultTemperature	double	Default thermostat temperature
//    securityEnabled	boolean	Global security setting
//    notificationsEnabled	boolean	Enable/disable notifications
//    autoModeEnabled	boolean	Enable/disable automatic scenarios
//
//    You can later add more settings if the project requires them.
//
//    For TemperatureUnit:
//
//    CELSIUS
//            FAHRENHEIT
//    Methods of HomeConfiguration
//
//    This class is basically a configuration object, so its methods are mostly getters/setters or specific configuration operations.
//
//    For example:
//
//    getTemperatureUnit()
//    setTemperatureUnit(...)
//
//    getDefaultTemperature()
//    setDefaultTemperature(...)
//
//    isSecurityEnabled()
//    setSecurityEnabled(...)
//
//    isNotificationsEnabled()
//    setNotificationsEnabled(...)
//
//    isAutoModeEnabled()
//    setAutoModeEnabled(...)
//
//    You don't need complicated methods here.
}
