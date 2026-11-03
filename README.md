# Smart Home Automation System

A pure-Java (21) smart-home application: rooms, devices, ecosystems and automation scenarios,
built around the five creational design patterns and exposed through a REST API.
There are **no external dependencies**. The API runs on the JDK's built-in `HttpServer`.

See [SYSTEM_ARCHITECTURE.md](SYSTEM_ARCHITECTURE.md) for the design.

## Run

```bash
javac -d out $(find src -name "*.java")
java -cp out com.smarthome.Main            # web dashboard on http://localhost:8080/ + REST API on /api, with demo data
java -cp out com.smarthome.Main cli        # interactive command line (type "help")
java -cp out com.smarthome.Main demo       # console walkthrough of the full workflow
```

Options:

- `--empty`: start without demo data. Works for the API and the CLI.
- `--port 9000` (or env `PORT`) and `--host 0.0.0.0` (or env `HOST`): API only.

The CLI does not run the scheduler, so `TIME` triggers only fire in API mode. In the CLI, run scenarios with `run <id>` or fire events with `event <name>`.

On Windows PowerShell, compile with:

```powershell
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
```

In IntelliJ, open the folder and run `com.smarthome.Main`.

## Web dashboard

Start the server and open **http://localhost:8080/** to use the dashboard. It's a single page (`web/index.html`) served by the same server, with plain HTML, CSS and JavaScript and no build step or dependencies. It refreshes every 3 seconds and has six tabs:

- **Rooms & devices:** live stats, plus one card per room with device controls. You can switch devices on and off, and set brightness, temperature, recording, fan speed, TV channel and volume, and locks. You can also add, rename, move and delete rooms and devices, and filter by type or status.
- **Scenarios:** run a scenario (with its execution report), create or edit one with a form-based Builder, clone it, enable or disable it, save it as a template, or delete it.
- **Templates:** add any built-in or saved template to your home.
- **Notifications:** the notification feed, with a button to clear it.
- **Home & settings:** temperature unit and default temperature, security, notifications, auto mode, firing events, renaming the home and changing its ecosystem, starting a new home, and a text snapshot of the home's state.
- **API explorer:** every endpoint with a description. Click one to fill in a ready-to-send request with real ids, then send it and see the status, timing and JSON response.

The header always shows the security state (click to arm or disarm), a °C/°F switch, a light/dark theme toggle and the connection status.

The server looks for `web/` in the current folder, then next to the `out/` folder, so it works from any directory. To use another folder, pass `-Dsmarthome.web=<folder>`.

## Tests

The tests have no dependencies. They cover the patterns, the services and an end-to-end HTTP run.

```bash
javac -d out $(find src test -name "*.java") && java -cp out com.smarthome.SmartHomeTests
```

## Design patterns

| Pattern          | Where                                                     | Problem it solves |
| ---------------- | --------------------------------------------------------- | ----------------- |
| Singleton        | `singleton/SmartHomeManager`                              | API, CLI and scheduler must all see the same house |
| Factory Method   | `factory/DeviceCreator` + `LightCreator`, `TVCreator`...  | Clients ask for a `DeviceType`, never `new Light(...)` |
| Abstract Factory | `abstractfactory/SmartHomeFactory` + Google/Apple/Amazon/Generic | A home's lights, thermostats and cameras all come from the chosen ecosystem |
| Builder          | `builder/AutomationScenarioBuilder`                       | Scenarios have many optional parts (trigger, description, N actions) |
| Prototype        | `prototype/Prototype`, `AutomationScenario.copy()`, `prototype/AutomationPrototype` (template registry) | "Movie Mode Winter" is cloned from "Movie Mode", new homes start from ready-made templates, and every copy stays independent |

## Concepts

- **Device types:** `LIGHT` (brightness), `THERMOSTAT` (target temperature 5-35 °C), `CAMERA` (recording), `FAN` (speed 1-5), `TV` (channel, volume), `DOOR_LOCK` (locked).
- **Action:** `{ "type": ActionType, "deviceId" | "deviceType", "roomId"?, "value"? }`. An action targets one device, or every device of a type (optionally limited to one room).
- **Action types:** `TURN_ON`, `TURN_OFF` (any device), `SET_BRIGHTNESS`, `SET_TEMPERATURE`, `START_RECORDING`, `STOP_RECORDING`, `SET_SPEED`, `SET_CHANNEL`, `SET_VOLUME`, `LOCK`, `UNLOCK`. `GET /api/meta` lists them with the device type each one applies to.
- **Trigger:** `MANUAL` (run through the API), `TIME` (`"value": "HH:mm"`, run by the scheduler) or `EVENT` (`"value": "<event name>"`, run by `POST /api/events`). `TIME` and `EVENT` triggers fire only while `autoModeEnabled` is `true`.
- **Execution report:** each action/device pair is `APPLIED`, `SKIPPED` (no matching device) or `FAILED` (invalid value, or blocked by security). One failure does not stop the other actions.
- **Temperature unit:** all temperatures are stored in Celsius. When `temperatureUnit` is `FAHRENHEIT`, every temperature the API or CLI accepts or returns is in Fahrenheit. This covers thermostat `targetTemperature`, `defaultTemperature`, and `SET_TEMPERATURE` values in commands and scenarios. The API also accepts `C` and `F`.
- **Security mode:** arming locks every door and starts every camera recording. While armed, the system refuses anything that would weaken protection (`UNLOCK`, `STOP_RECORDING`, turning a lock or camera off) with a `422` error, until you disarm. You can arm with `POST /api/home/security/arm`, with `"securityEnabled": true` in the configuration, or from a scenario.
- **Scenario security mode:** set `"securityMode"` to `NONE`, `ARM` or `DISARM`. `DISARM` runs *before* the actions, so Good Morning can disarm and then unlock the doors. `ARM` runs *after* the actions.
- **Notifications:** while `notificationsEnabled` is on, the system records a notification each time a scenario runs, security is armed or disarmed, or an action is blocked. A scenario can set its own text (`notificationMessage`) or stay silent (`"notifyOnExecution": false`). Read them with `GET /api/notifications`, newest first; the last 200 are kept.
- **Templates:** five built-in templates (`good-night`, `good-morning`, `movie-mode`, `away`, `welcome-home`) target devices by type, so they work in any home. You can also save your own scenarios as templates.

Enum values are case-insensitive, and dashes are accepted (`"door-lock"`).

## REST API

Base URL: `http://localhost:8080/api`. `GET /api` lists every endpoint.

### Home
| Method | Path | Body / query |
| ------ | ---- | ------------ |
| GET  | `/api/health` | |
| GET  | `/api/meta` | all accepted enum values |
| GET  | `/api/home` | home summary with rooms and devices |
| POST | `/api/home` | `{"name","ecosystem"?}`: creates a new empty home that **replaces** the current one |
| PUT  | `/api/home` | `{"name"?,"ecosystem"?}`: a new ecosystem applies to devices created afterwards |
| GET  | `/api/home/state` | text snapshot |
| GET / PUT | `/api/home/configuration` | `{"temperatureUnit"?,"defaultTemperature"?,"securityEnabled"?,"notificationsEnabled"?,"autoModeEnabled"?}`. `defaultTemperature` is read in the unit sent in the same request |
| POST | `/api/home/security/arm` · `/disarm` | |
| GET / DELETE | `/api/notifications` | list (newest first) / clear |

### Rooms
| Method | Path | Body |
| ------ | ---- | ---- |
| GET    | `/api/rooms` | |
| POST   | `/api/rooms` | `{"name","type"}` |
| GET / PUT / DELETE | `/api/rooms/{id}` | PUT: `{"name"?,"type"?}`. DELETE also removes the room's devices |
| GET    | `/api/rooms/{id}/devices` | |
| POST   | `/api/rooms/{id}/devices` | `{"type","name"}` |

### Devices
| Method | Path | Body / query |
| ------ | ---- | ------------ |
| GET    | `/api/devices` | `?type=LIGHT&roomId=room-1&status=ON` (all optional) |
| POST   | `/api/devices` | `{"roomId","type","name"}` |
| GET / PUT / DELETE | `/api/devices/{id}` | PUT: `{"name"?,"roomId"?}` (`roomId` moves the device) |
| POST   | `/api/devices/{id}/on` · `/off` | |
| POST   | `/api/devices/{id}/commands` | `{"type":"SET_BRIGHTNESS","value":40}` |

### Scenarios
| Method | Path | Body |
| ------ | ---- | ---- |
| GET    | `/api/scenarios` | |
| POST   | `/api/scenarios` | `{"name","description"?,"enabled"?,"trigger"?,"actions":[...],"securityMode"?,"notifyOnExecution"?,"notificationMessage"?}` |
| GET / PUT / DELETE | `/api/scenarios/{id}` | PUT: any of the POST fields (`actions` replaces the whole list) |
| POST   | `/api/scenarios/{id}/execute` | returns the execution report |
| POST   | `/api/scenarios/{id}/clone` | `{"name"?}` |
| POST   | `/api/scenarios/{id}/enable` · `/disable` | |
| POST   | `/api/scenarios/{id}/save-as-template` | `{"key":"my-night"}` |
| POST   | `/api/events` | `{"name":"arrived-home"}`: runs the matching EVENT scenarios (auto mode only) |

### Templates
| Method | Path | Body |
| ------ | ---- | ---- |
| GET    | `/api/templates` | |
| POST   | `/api/templates/{key}/create` | `{"name"?}`: adds a new scenario to the home, cloned from the template |

### Errors

```json
{ "status": 404, "error": "Not Found", "message": "Device not found: dev-99" }
```

The API uses these status codes:

- `400`: invalid JSON, a missing field or a bad value.
- `404`: unknown resource or endpoint.
- `405`: the method is not allowed on that path.
- `422`: a valid request that breaks a rule. Examples are running a disabled scenario, sending `LOCK` to a light, or unlocking a door while security is armed.

### Example

```bash
curl -X POST localhost:8080/api/rooms -H "Content-Type: application/json" \
     -d '{"name":"Office","type":"OFFICE"}'

curl -X POST localhost:8080/api/scenarios -H "Content-Type: application/json" -d '{
  "name": "Good Night",
  "trigger": {"type": "TIME", "value": "22:30"},
  "actions": [
    {"type": "TURN_OFF", "deviceType": "LIGHT"},
    {"type": "LOCK", "deviceType": "DOOR_LOCK"},
    {"type": "SET_TEMPERATURE", "deviceType": "THERMOSTAT", "value": 19}
  ]}'

curl -X POST localhost:8080/api/scenarios/scn-1/execute
```

## CLI

```text
> rooms
> device add room-1 LIGHT Desk Lamp
> cmd dev-1 SET_BRIGHTNESS 40
> set unit F
> scenario new          (guided: name, trigger, security, notification, then one action per line)
> template away Leaving
> arm
> notifications
```

Type `help` for the full list of commands.

State is kept in memory. Restarting the server resets it to the demo data.
