# Smart Home Automation System

A pure-Java (21) smart-home application: rooms, devices, ecosystems and automation scenarios,
built around the five creational design patterns and exposed through a REST API.
There are **no external dependencies**. The API runs on the JDK's built-in `HttpServer`.

See [SYSTEM_ARCHITECTURE.md](SYSTEM_ARCHITECTURE.md) for the design.

## Run

```bash
javac -d out $(find src -name "*.java")
java -cp out com.smarthome.Main            # REST API on http://localhost:8080/api, with demo data
java -cp out com.smarthome.Main demo       # console walkthrough of the full workflow
```

Server options: `--port 9000` (or env `PORT`), `--host 0.0.0.0` (or env `HOST`), `--empty` (start without demo data).

On Windows PowerShell, compile with:

```powershell
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
```

In IntelliJ, open the folder and run `com.smarthome.Main`.

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
| Prototype        | `prototype/Prototype`, `AutomationScenario.copy()`        | "Movie Mode Winter" is cloned from "Movie Mode", and the two stay independent |

## Concepts

- **Device types:** `LIGHT` (brightness), `THERMOSTAT` (target temperature 5-35 °C), `CAMERA` (recording), `FAN` (speed 1-5), `TV` (channel, volume), `DOOR_LOCK` (locked).
- **Action:** `{ "type": ActionType, "deviceId" | "deviceType", "roomId"?, "value"? }`. An action targets one device, or every device of a type (optionally limited to one room).
- **Action types:** `TURN_ON`, `TURN_OFF` (any device), `SET_BRIGHTNESS`, `SET_TEMPERATURE`, `START_RECORDING`, `STOP_RECORDING`, `SET_SPEED`, `SET_CHANNEL`, `SET_VOLUME`, `LOCK`, `UNLOCK`. `GET /api/meta` lists them with the device type each one applies to.
- **Trigger:** `MANUAL` (run through the API), `TIME` (`"value": "HH:mm"`, run by the scheduler) or `EVENT` (`"value": "<event name>"`, run by `POST /api/events`). `TIME` and `EVENT` triggers fire only while `autoModeEnabled` is `true`.
- **Execution report:** each action/device pair is `APPLIED`, `SKIPPED` (no matching device) or `FAILED` (invalid value). One failure does not stop the other actions.

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
| GET / PUT | `/api/home/configuration` | `{"temperatureUnit"?,"defaultTemperature"?,"securityEnabled"?,"notificationsEnabled"?,"autoModeEnabled"?}` |

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
| POST   | `/api/scenarios` | `{"name","description"?,"enabled"?,"trigger"?,"actions":[...]}` |
| GET / PUT / DELETE | `/api/scenarios/{id}` | PUT: any of the POST fields (`actions` replaces the whole list) |
| POST   | `/api/scenarios/{id}/execute` | returns the execution report |
| POST   | `/api/scenarios/{id}/clone` | `{"name"?}` |
| POST   | `/api/scenarios/{id}/enable` · `/disable` | |
| POST   | `/api/events` | `{"name":"arrived-home"}`: runs the matching EVENT scenarios (auto mode only) |

### Errors

```json
{ "status": 404, "error": "Not Found", "message": "Device not found: dev-99" }
```

The API uses these status codes:

- `400`: invalid JSON, a missing field or a bad value.
- `404`: unknown resource or endpoint.
- `405`: the method is not allowed on that path.
- `422`: a valid request that breaks a rule. Examples are running a disabled scenario or sending `LOCK` to a light.

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

State is kept in memory. Restarting the server resets it to the demo data.
