# Smart Home Automation System

## 1. Project Overview

The Smart Home Automation System is a Java application designed to manage and automate devices inside a smart home.

The system allows users to:

* Manage rooms.
* Add and manage smart devices.
* Choose a smart-home ecosystem.
* Create automation scenarios.
* Execute automation scenarios.
* Clone existing scenarios.
* Manage the global state of the smart home.

The project is primarily designed as a learning project for practicing Object-Oriented Programming, SOLID principles, and the following Creational Design Patterns:

* Singleton
* Factory Method
* Abstract Factory
* Builder
* Prototype

The first version will be implemented using pure Java without Spring Boot. Persistence and a REST API can be added later.

---

# 2. Main System Architecture

The system follows a layered architecture.

```text
┌──────────────────────────────────────────────────────┐
│                    PRESENTATION                      │
│                                                      │
│                    CLI / Main                        │
│                                                      │
└──────────────────────────┬───────────────────────────┘
                           │
                           ↓
┌──────────────────────────────────────────────────────┐
│                    APPLICATION                       │
│                                                      │
│  SmartHomeService                                    │
│  DeviceService                                       │
│  RoomService                                         │
│  AutomationService                                   │
│                                                      │
└──────────────────────────┬───────────────────────────┘
                           │
                           ↓
┌──────────────────────────────────────────────────────┐
│                       DOMAIN                         │
│                                                      │
│  SmartHome                                           │
│  Room                                                │
│  Device                                              │
│  Light                                               │
│  Thermostat                                          │
│  Camera                                              │
│  Fan                                                 │
│  TV                                                  │
│  DoorLock                                            │
│  AutomationScenario                                  │
│                                                      │
└──────────────────────────┬───────────────────────────┘
                           │
                           ↓
┌──────────────────────────────────────────────────────┐
│                  CREATIONAL PATTERNS                 │
│                                                      │
│  Singleton                                           │
│  Factory Method                                      │
│  Abstract Factory                                    │
│  Builder                                             │
│  Prototype                                           │
│                                                      │
└──────────────────────────────────────────────────────┘
```

---

# 3. Package Structure

The initial Java project should be organized as follows:

```text
src/
└── com/
    └── smarthome/
        │
        ├── Main.java
        │
        ├── model/
        │   ├── SmartHome.java
        │   ├── Room.java
        │   ├── Device.java
        │   │
        │   ├── Light.java
        │   ├── Thermostat.java
        │   ├── Camera.java
        │   ├── Fan.java
        │   ├── TV.java
        │   └── DoorLock.java
        │
        ├── singleton/
        │   └── SmartHomeManager.java
        │
        ├── factory/
        │   ├── DeviceCreator.java
        │   ├── LightCreator.java
        │   ├── ThermostatCreator.java
        │   ├── CameraCreator.java
        │   └── ...
        │
        ├── abstractfactory/
        │   │
        │   ├── SmartHomeFactory.java
        │   │
        │   ├── google/
        │   │   ├── GoogleHomeFactory.java
        │   │   ├── GoogleLight.java
        │   │   ├── GoogleThermostat.java
        │   │   └── GoogleCamera.java
        │   │
        │   ├── apple/
        │   │   ├── AppleHomeFactory.java
        │   │   ├── AppleLight.java
        │   │   ├── AppleThermostat.java
        │   │   └── AppleCamera.java
        │   │
        │   └── amazon/
        │       ├── AmazonHomeFactory.java
        │       ├── AmazonLight.java
        │       ├── AmazonThermostat.java
        │       └── AmazonCamera.java
        │
        ├── builder/
        │   └── AutomationScenarioBuilder.java
        │
        ├── prototype/
        │   ├── Prototype.java
        │   └── AutomationPrototype.java
        │
        ├── service/
        │   ├── SmartHomeService.java
        │   ├── DeviceService.java
        │   ├── RoomService.java
        │   └── AutomationService.java
        │
        └── exception/
            ├── DeviceNotFoundException.java
            ├── RoomNotFoundException.java
            └── InvalidAutomationException.java
```

---

# 4. Domain Layer

The domain layer represents the actual objects in the smart-home system.

## 4.1 SmartHome

Represents the entire house.

Responsibilities:

* Store rooms.
* Manage devices.
* Retrieve rooms.
* Retrieve devices.
* Track active automation.
* Represent the current state of the home.

Relationship:

```text
SmartHome
    │
    ├── Room
    │    ├── Device
    │    ├── Device
    │    └── Device
    │
    ├── Room
    │    ├── Device
    │    └── Device
    │
    └── Room
         └── Device
```

---

# 5. Device Hierarchy

All smart devices share common behavior.

```text
                    Device
                       │
        ┌──────────────┼──────────────┐
        │              │              │
      Light        Thermostat        Fan
        │
        ├── Camera
        ├── TV
        └── DoorLock
```

The exact inheritance structure can be refined during implementation.

Every device should have concepts such as:

* identifier
* name
* status
* room
* manufacturer/ecosystem

Common behavior includes:

* turning on
* turning off
* checking status

Concrete devices add their own specific behavior.

---

# 6. Singleton Pattern

## Purpose

The application needs one central manager responsible for the global Smart Home state.

The Singleton ensures that only one instance of this manager exists.

```text
                 SmartHomeManager
                      Singleton
                         │
             ┌───────────┴───────────┐
             │                       │
          RoomManager           DeviceManager
             │                       │
             └───────────┬───────────┘
                         ↓
                     SmartHome
```

## Responsibilities

The Singleton manager is responsible for:

* accessing the current SmartHome.
* global application configuration.
* registering rooms.
* registering devices.
* accessing global state.

## Design Constraint

The manager must:

* have a private constructor.
* maintain one static instance.
* provide a global access method.
* return the same instance every time.

---

# 7. Factory Method Pattern

## Problem

The application needs to create different types of devices.

Examples:

```text
Light
Thermostat
Camera
Fan
TV
DoorLock
```

The client should not need to know the concrete implementation used to create the device.

## Responsibility

Factory Method is responsible for deciding which concrete device should be created.

Conceptually:

```text
                 Device
                   ↑
                   │
             Device Creator
                   │
        ┌──────────┼──────────┐
        ↓          ↓          ↓
     Light      Thermostat   Camera
```

## Example Workflow

The application receives:

```text
Device Type = LIGHT
```

The creation mechanism determines that a Light object is required.

The client works with the `Device` abstraction rather than directly depending on the concrete Light implementation.

---

# 8. Abstract Factory Pattern

## Problem

The application supports multiple smart-home ecosystems.

For example:

```text
Google
Apple
Amazon
```

Each ecosystem provides a compatible family of devices.

## Product Families

Each factory can create:

```text
Light
Thermostat
Camera
```

Therefore:

```text
                SmartHomeFactory
                       │
          ┌────────────┼────────────┐
          ↓            ↓            ↓
       Light       Thermostat     Camera
```

Concrete factories:

```text
GoogleHomeFactory
       │
       ├── GoogleLight
       ├── GoogleThermostat
       └── GoogleCamera
```

```text
AppleHomeFactory
       │
       ├── AppleLight
       ├── AppleThermostat
       └── AppleCamera
```

```text
AmazonHomeFactory
       │
       ├── AmazonLight
       ├── AmazonThermostat
       └── AmazonCamera
```

## Main Objective

The client chooses an ecosystem:

```text
Google
```

and receives a complete compatible family.

The client should not manually create:

```text
GoogleLight
GoogleThermostat
GoogleCamera
```

---

# 9. Builder Pattern

## Problem

An automation scenario can contain many parameters and actions.

Example:

```text
AutomationScenario

name
description
trigger
schedule
actions
temperature
lighting configuration
security configuration
notification configuration
```

Some parameters are required.

Others are optional.

## Solution

The Builder constructs the scenario progressively.

Conceptually:

```text
AutomationScenarioBuilder
          │
          ├── name
          ├── description
          ├── trigger
          ├── schedule
          ├── addAction()
          ├── temperature
          ├── security settings
          │
          ↓
        build()
          │
          ↓
AutomationScenario
```

## Example Scenarios

### Good Night

```text
Lights → OFF
TV → OFF
Doors → LOCK
Camera → ON
Thermostat → 19°C
```

### Good Morning

```text
Bedroom Light → ON
Kitchen Light → ON
Thermostat → 21°C
```

### Movie Mode

```text
Living Room Light → 20%
TV → ON
Thermostat → 21°C
Door → LOCK
```

---

# 10. Prototype Pattern

## Problem

Users may create complex automation scenarios and then want similar versions.

For example:

```text
Movie Mode
    │
    ├── Movie Mode Summer
    ├── Movie Mode Winter
    ├── Movie Mode Family
    └── Movie Mode Horror
```

Rebuilding each scenario from zero is unnecessary.

## Solution

An existing AutomationScenario can act as a prototype.

The system creates new scenarios by cloning the existing prototype.

```text
                  Movie Mode
                  Prototype
                      │
          ┌───────────┼───────────┐
          ↓           ↓           ↓
       Clone A      Clone B     Clone C
```

Each clone must become an independent object.

Changing a clone should not unintentionally modify the original prototype.

---

# 11. Service Layer

The service layer contains application/business operations.

## SmartHomeService

Responsible for operations such as:

* create a smart home.
* add rooms.
* remove rooms.
* retrieve rooms.
* display home state.

---

## DeviceService

Responsible for:

* adding devices.
* removing devices.
* searching devices.
* turning devices on/off.
* displaying device status.

---

## RoomService

Responsible for:

* creating rooms.
* adding devices to rooms.
* removing devices.
* listing devices in a room.

---

## AutomationService

Responsible for:

* creating automation scenarios.
* validating scenarios.
* executing scenarios.
* cloning scenarios.
* activating/deactivating automations.

---

# 12. Main Application Flow

The user interacts with the CLI.

```text
                         Main
                          │
                          ↓
                    SmartHomeManager
                      Singleton
                          │
                          ↓
                       Services
                          │
             ┌────────────┼────────────┐
             ↓            ↓            ↓
          Devices       Rooms      Automation
             │                         │
             ↓                         ↓
      Factory/Factory            Builder/Prototype
        Method
             │
             ↓
      Abstract Factory
             │
             ↓
        Device Family
```

---

# 13. Example Complete Workflow

A typical user session could be:

```text
1. Start application
        ↓
2. Get SmartHomeManager
        ↓
3. Create SmartHome
        ↓
4. Create rooms
        ↓
5. Select ecosystem
        ↓
6. Abstract Factory creates compatible devices
        ↓
7. Add devices to rooms
        ↓
8. Create automation scenario
        ↓
9. Builder constructs scenario
        ↓
10. Save scenario
        ↓
11. Clone scenario when necessary
        ↓
12. Execute automation
        ↓
13. Devices change their state
```

---

# 14. Creational Pattern Map

| Pattern          | Project Responsibility                |
| ---------------- | ------------------------------------- |
| Singleton        | Global SmartHomeManager               |
| Factory Method   | Device creation                       |
| Abstract Factory | Compatible ecosystem device families  |
| Builder          | Complex automation scenarios          |
| Prototype        | Cloning existing automation scenarios |

---

# 15. Important Design Rule

The project must not use a pattern simply because the project requires five patterns.

Each pattern must solve a real design problem.

Before implementing a pattern, answer:

### Singleton

"What problem exists if multiple SmartHomeManagers are created?"

### Factory Method

"Why shouldn't the client directly instantiate every Device implementation?"

### Abstract Factory

"Why do we need compatible families of devices?"

### Builder

"Why is creating an AutomationScenario directly becoming difficult?"

### Prototype

"Why would cloning an existing scenario be better than creating one from zero?"

If you cannot answer these questions, do not implement the pattern yet.

---

# 16. Future Architecture

After the pure Java version is complete, the project can evolve into:

```text
                    Client
                      │
                      ↓
                REST API
                      │
                      ↓
                Spring Boot
                      │
        ┌─────────────┼─────────────┐
        ↓             ↓             ↓
    Controllers     Services    Security
                      │
                      ↓
                  Domain
                      │
                      ↓
               Design Patterns
                      │
                      ↓
                 Repository
                      │
                      ↓
                  Database
```

Possible future technologies:

* Java
* Spring Boot
* Spring Data JPA
* PostgreSQL
* Docker
* REST API
* JUnit
* Mockito

However, these technologies should be introduced **after the pure Java version is understood**.
