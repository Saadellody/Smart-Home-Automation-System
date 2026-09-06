package com.smartphone.model;

public class HomeConfiguration {

    private HomeConfiguration homeConfiguration;
    private double defaultTemperature;
    private boolean securityEnabled;
    private boolean notificationsEnabled;
    private boolean autoModeEnabled;

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
