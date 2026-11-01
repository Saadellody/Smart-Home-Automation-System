package com.smarthome.cli;

import com.smarthome.builder.AutomationScenarioBuilder;
import com.smarthome.model.Action;
import com.smarthome.model.AutomationScenario;
import com.smarthome.model.Device;
import com.smarthome.model.ExecutionReport;
import com.smarthome.model.HomeConfiguration;
import com.smarthome.model.Room;
import com.smarthome.model.Trigger;
import com.smarthome.model.enums.ActionType;
import com.smarthome.model.enums.DeviceType;
import com.smarthome.model.enums.Ecosystem;
import com.smarthome.model.enums.RoomType;
import com.smarthome.model.enums.SecurityAction;
import com.smarthome.model.enums.TemperatureUnit;
import com.smarthome.service.DeviceService;
import com.smarthome.service.Services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Interactive command-line interface (the presentation layer of SYSTEM_ARCHITECTURE.md).
 * Reads one command per line; type {@code help} to list them.
 */
public class CommandLineInterface {

    private static final String HELP = """
            Home
              state                                  show the whole home
              home new <ECOSYSTEM> <name...>         start a new empty home (GENERIC, GOOGLE, APPLE, AMAZON)
              ecosystem <ECOSYSTEM>                  ecosystem for devices created from now on
              config                                 show the configuration
              set unit <C|F>                         temperature unit for display and input
              set default-temp <value>               default temperature of new thermostats
              set notifications <on|off>
              set automode <on|off>                  let TIME and EVENT triggers run automatically
              arm | disarm                           security mode
              notifications [clear]
              demo                                   load the demo home
            Rooms
              rooms
              room add <TYPE> <name...>              LIVING_ROOM, BEDROOM, KITCHEN, BATHROOM, GARAGE, OFFICE
              room rename <roomId> <name...>
              room rm <roomId>                       also removes its devices
            Devices
              devices [TYPE]
              device add <roomId> <TYPE> <name...>   LIGHT, THERMOSTAT, CAMERA, FAN, TV, DOOR_LOCK
              device rename <deviceId> <name...>
              device move <deviceId> <roomId>
              device rm <deviceId>
              on <deviceId> | off <deviceId>
              cmd <deviceId> <ACTION> [value]        e.g. cmd dev-1 SET_BRIGHTNESS 40, cmd dev-8 UNLOCK
            Scenarios
              scenarios
              scenario show <id>
              scenario new                           guided creation (Builder)
              scenario rm <id>
              run <id>
              clone <id> [name...]                   (Prototype)
              enable <id> | disable <id>
              templates
              template <key> [name...]               add a scenario from a template
              save-template <scenarioId> <key>
              event <name>                           fire an event for EVENT triggers
            help | exit
            """;

    private final Services services;
    private final BufferedReader in;
    private final PrintStream out;

    public CommandLineInterface(Services services, BufferedReader in, PrintStream out) {
        this.services = services;
        this.in = in;
        this.out = out;
    }

    public void run() throws IOException {
        out.println("Smart Home Automation System. Type 'help' for commands, 'exit' to quit.");
        while (true) {
            out.print("> ");
            out.flush();
            String line = in.readLine();
            if (line == null) {
                return;
            }
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.equals("exit") || line.equals("quit")) {
                out.println("Bye.");
                return;
            }
            try {
                execute(line.split("\\s+"));
            } catch (RuntimeException e) {
                out.println("Error: " + e.getMessage());
            }
        }
    }

    private void execute(String[] args) throws IOException {
        String command = args[0].toLowerCase(Locale.ROOT);
        switch (command) {
            case "help" -> out.print(HELP);
            case "state" -> out.println(services.homes().describeState());
            case "demo" -> {
                DemoData.seed(services);
                out.println("Demo home loaded.");
                out.println(services.homes().describeState());
            }
            case "home" -> {
                require(args, 4, "home new <ECOSYSTEM> <name...>");
                services.homes().createHome(rest(args, 3), parse(Ecosystem.class, args[2]));
                out.println("New home created.");
            }
            case "ecosystem" -> {
                require(args, 2, "ecosystem <ECOSYSTEM>");
                services.homes().updateHome(null, parse(Ecosystem.class, args[1]));
                out.println("New devices will use the " + services.homes().getHome().getEcosystem() + " ecosystem.");
            }
            case "config" -> printConfig();
            case "set" -> set(args);
            case "arm" -> {
                services.homes().armSecurity();
                out.println("Security armed: doors locked, cameras recording.");
            }
            case "disarm" -> {
                services.homes().disarmSecurity();
                out.println("Security disarmed.");
            }
            case "notifications" -> {
                if (args.length > 1 && args[1].equalsIgnoreCase("clear")) {
                    services.notifications().clear();
                    out.println("Notifications cleared.");
                } else if (services.notifications().list().isEmpty()) {
                    out.println("No notifications.");
                } else {
                    services.notifications().list().forEach(n ->
                            out.println(n.time() + "  " + n.type() + "  " + n.message()));
                }
            }
            case "rooms" -> listRooms();
            case "room" -> room(args);
            case "devices" -> {
                DeviceType type = args.length > 1 ? parse(DeviceType.class, args[1]) : null;
                printDevices(services.devices().listDevices(type, null, null));
            }
            case "device" -> device(args);
            case "on" -> {
                require(args, 2, "on <deviceId>");
                printDevice(services.devices().turnOn(args[1]));
            }
            case "off" -> {
                require(args, 2, "off <deviceId>");
                printDevice(services.devices().turnOff(args[1]));
            }
            case "cmd" -> {
                require(args, 3, "cmd <deviceId> <ACTION> [value]");
                ActionType type = parse(ActionType.class, args[2]);
                Double value = args.length > 3 ? toCelsiusIfTemperature(type, number(args[3])) : null;
                printDevice(services.devices().executeCommand(args[1], type, value));
            }
            case "scenarios" -> services.automations().listScenarios().forEach(this::printScenarioLine);
            case "scenario" -> scenario(args);
            case "run" -> {
                require(args, 2, "run <scenarioId>");
                out.println(formatReport(services.automations().execute(args[1])));
            }
            case "clone" -> {
                require(args, 2, "clone <scenarioId> [name...]");
                printScenarioLine(services.automations().cloneScenario(args[1], args.length > 2 ? rest(args, 2) : null));
            }
            case "enable" -> {
                require(args, 2, "enable <scenarioId>");
                printScenarioLine(services.automations().enable(args[1]));
            }
            case "disable" -> {
                require(args, 2, "disable <scenarioId>");
                printScenarioLine(services.automations().disable(args[1]));
            }
            case "templates" -> services.automations().listTemplates().forEach((key, s) ->
                    out.println(key + "  " + s.getName() + "  - " + s.getDescription()));
            case "template" -> {
                require(args, 2, "template <key> [name...]");
                printScenarioLine(services.automations().createFromTemplate(args[1], args.length > 2 ? rest(args, 2) : null));
            }
            case "save-template" -> {
                require(args, 3, "save-template <scenarioId> <key>");
                services.automations().saveAsTemplate(args[1], args[2]);
                out.println("Saved as template '" + args[2] + "'.");
            }
            case "event" -> {
                require(args, 2, "event <name>");
                List<ExecutionReport> reports = services.automations().fireEvent(rest(args, 1));
                if (reports.isEmpty()) {
                    out.println("No scenario ran (is auto mode on, and is a scenario listening to this event?).");
                }
                reports.forEach(r -> out.println(formatReport(r)));
            }
            default -> out.println("Unknown command '" + args[0] + "'. Type 'help'.");
        }
    }

    // ---------------------------------------------------------------- home / config

    private void set(String[] args) {
        require(args, 3, "set <unit|default-temp|notifications|automode> <value>");
        String value = args[2];
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "unit" -> services.homes().updateConfiguration(unit(value), null, null, null, null);
            case "default-temp" -> services.homes().updateConfiguration(null, number(value), null, null, null);
            case "notifications" -> services.homes().updateConfiguration(null, null, null, onOff(value), null);
            case "automode" -> services.homes().updateConfiguration(null, null, null, null, onOff(value));
            default -> throw new IllegalArgumentException("Unknown setting '" + args[1] + "'");
        }
        printConfig();
    }

    private void printConfig() {
        HomeConfiguration c = services.homes().getConfiguration();
        String symbol = c.getTemperatureUnit().getSymbol();
        out.println("unit=" + symbol
                + "  default-temp=" + c.fromCelsius(c.getDefaultTemperature()) + " " + symbol
                + "  security=" + (c.isSecurityEnabled() ? "armed" : "disarmed")
                + "  notifications=" + (c.isNotificationsEnabled() ? "on" : "off")
                + "  automode=" + (c.isAutoModeEnabled() ? "on" : "off"));
    }

    // ---------------------------------------------------------------- rooms

    private void room(String[] args) {
        require(args, 3, "room <add|rename|rm> ...");
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "add" -> {
                require(args, 4, "room add <TYPE> <name...>");
                Room room = services.rooms().createRoom(rest(args, 3), parse(RoomType.class, args[2]));
                out.println("Created " + room.getId() + " (" + room.getName() + ")");
            }
            case "rename" -> {
                require(args, 4, "room rename <roomId> <name...>");
                services.rooms().updateRoom(args[2], rest(args, 3), null);
                out.println("Renamed.");
            }
            case "rm" -> {
                services.rooms().deleteRoom(args[2]);
                out.println("Removed " + args[2] + ".");
            }
            default -> throw new IllegalArgumentException("Unknown room command '" + args[1] + "'");
        }
    }

    private void listRooms() {
        for (Room room : services.rooms().listRooms()) {
            out.println(room.getId() + "  " + room.getName() + "  [" + room.getType() + "]  "
                    + room.getDevices().size() + " device(s)");
        }
    }

    // ---------------------------------------------------------------- devices

    private void device(String[] args) {
        require(args, 3, "device <add|rename|move|rm> ...");
        DeviceService devices = services.devices();
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "add" -> {
                require(args, 5, "device add <roomId> <TYPE> <name...>");
                printDevice(devices.addDevice(args[2], parse(DeviceType.class, args[3]), rest(args, 4)));
            }
            case "rename" -> {
                require(args, 4, "device rename <deviceId> <name...>");
                printDevice(devices.renameDevice(args[2], rest(args, 3)));
            }
            case "move" -> {
                require(args, 4, "device move <deviceId> <roomId>");
                printDevice(devices.moveDevice(args[2], args[3]));
            }
            case "rm" -> {
                devices.removeDevice(args[2]);
                out.println("Removed " + args[2] + ".");
            }
            default -> throw new IllegalArgumentException("Unknown device command '" + args[1] + "'");
        }
    }

    private void printDevices(List<Device> devices) {
        if (devices.isEmpty()) {
            out.println("No devices.");
        }
        devices.forEach(this::printDevice);
    }

    private void printDevice(Device d) {
        out.println(d.getId() + "  " + d.getName() + "  " + d.getType() + "  in " + d.getRoom().getName()
                + "  (" + d.getModel() + ")  " + DeviceService.describeState(d, services.homes().getConfiguration()));
    }

    // ---------------------------------------------------------------- scenarios

    private void scenario(String[] args) throws IOException {
        require(args, 2, "scenario <show|new|rm> ...");
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "new" -> createScenarioInteractively();
            case "show" -> {
                require(args, 3, "scenario show <id>");
                printScenario(services.automations().getScenario(args[2]));
            }
            case "rm" -> {
                require(args, 3, "scenario rm <id>");
                services.automations().deleteScenario(args[2]);
                out.println("Removed " + args[2] + ".");
            }
            default -> throw new IllegalArgumentException("Unknown scenario command '" + args[1] + "'");
        }
    }

    private void createScenarioInteractively() throws IOException {
        AutomationScenarioBuilder builder = AutomationScenarioBuilder.scenario(ask("Name: "));
        String description = ask("Description (optional): ");
        if (!description.isEmpty()) {
            builder.description(description);
        }
        builder.trigger(parseTrigger(ask("Trigger [manual | HH:mm | event <name>] (manual): ")));
        String security = ask("Security [none | arm | disarm] (none): ");
        if (!security.isEmpty()) {
            builder.security(parse(SecurityAction.class, security));
        }
        String message = ask("Notification message (optional, 'off' for none): ");
        if (message.equalsIgnoreCase("off")) {
            builder.silent();
        } else if (!message.isEmpty()) {
            builder.notifyWith(message);
        }
        out.println("Actions, one per line: <deviceId | DEVICE_TYPE> <ACTION> [value]. Empty line to finish.");
        out.println("  e.g.  dev-1 SET_BRIGHTNESS 30     LIGHT TURN_OFF     THERMOSTAT SET_TEMPERATURE 20");
        while (true) {
            String line = ask("  action: ");
            if (line.isEmpty()) {
                break;
            }
            try {
                builder.addAction(parseAction(line.split("\\s+")));
            } catch (RuntimeException e) {
                out.println("  Error: " + e.getMessage());
            }
        }
        AutomationScenario scenario = services.automations().createScenario(builder);
        out.println("Created:");
        printScenario(scenario);
    }

    private Action parseAction(String[] parts) {
        if (parts.length < 2) {
            throw new IllegalArgumentException("Expected <deviceId | DEVICE_TYPE> <ACTION> [value]");
        }
        ActionType type = parse(ActionType.class, parts[1]);
        Double value = parts.length > 2 ? toCelsiusIfTemperature(type, number(parts[2])) : null;
        boolean isDevice = services.homes().getHome().findDeviceById(parts[0]).isPresent();
        return isDevice
                ? Action.forDevice(parts[0], type, value)
                : Action.forAll(parse(DeviceType.class, parts[0]), type, value);
    }

    private static Trigger parseTrigger(String text) {
        if (text.isEmpty() || text.equalsIgnoreCase("manual")) {
            return Trigger.manual();
        }
        if (text.toLowerCase(Locale.ROOT).startsWith("event ")) {
            return Trigger.onEvent(text.substring(6).trim());
        }
        return Trigger.at(text);
    }

    private void printScenarioLine(AutomationScenario s) {
        out.println(s.getId() + "  " + s.getName() + "  " + (s.isEnabled() ? "enabled" : "DISABLED")
                + "  trigger=" + describe(s.getTrigger()) + "  security=" + s.getSecurityAction()
                + "  actions=" + s.getActions().size());
    }

    private void printScenario(AutomationScenario s) {
        printScenarioLine(s);
        if (s.getDescription() != null) {
            out.println("  " + s.getDescription());
        }
        HomeConfiguration config = services.homes().getConfiguration();
        for (Action a : s.getActions()) {
            String target = a.deviceId() != null ? a.deviceId() : "all " + a.deviceType()
                    + (a.roomId() != null ? " in " + a.roomId() : "");
            String value = a.value() == null ? ""
                    : a.type() == ActionType.SET_TEMPERATURE
                    ? " " + config.fromCelsius(a.value()) + " " + config.getTemperatureUnit().getSymbol()
                    : " " + a.value();
            out.println("  - " + target + " -> " + a.type() + value);
        }
        out.println("  notify: " + (!s.isNotifyOnExecution() ? "off"
                : s.getNotificationMessage() != null ? "\"" + s.getNotificationMessage() + "\"" : "summary"));
    }

    private static String describe(Trigger t) {
        return t.value() == null ? t.type().name() : t.type() + " " + t.value();
    }

    /** One line per report entry; also used by {@link ConsoleDemo}. */
    static String formatReport(ExecutionReport report) {
        StringBuilder sb = new StringBuilder("'" + report.getScenarioName() + "': " + report.summary());
        for (ExecutionReport.Entry e : report.getEntries()) {
            sb.append("\n  ").append(e.outcome()).append("  ").append(e.action());
            if (e.deviceName() != null) {
                sb.append("  (").append(e.deviceName()).append(")");
            }
            if (e.message() != null) {
                sb.append("  ").append(e.message());
            }
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------- parsing helpers

    private String ask(String prompt) throws IOException {
        out.print(prompt);
        out.flush();
        String line = in.readLine();
        return line == null ? "" : line.trim();
    }

    private Double toCelsiusIfTemperature(ActionType type, double value) {
        return type == ActionType.SET_TEMPERATURE ? services.homes().getConfiguration().toCelsius(value) : value;
    }

    private static void require(String[] args, int count, String usage) {
        if (args.length < count) {
            throw new IllegalArgumentException("Usage: " + usage);
        }
    }

    private static String rest(String[] args, int from) {
        return String.join(" ", Arrays.copyOfRange(args, from, args.length));
    }

    private static double number(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + text + "' is not a number");
        }
    }

    private static boolean onOff(String text) {
        return switch (text.toLowerCase(Locale.ROOT)) {
            case "on", "true", "yes" -> true;
            case "off", "false", "no" -> false;
            default -> throw new IllegalArgumentException("Expected on or off");
        };
    }

    private static TemperatureUnit unit(String text) {
        Map<String, TemperatureUnit> aliases = Map.of("C", TemperatureUnit.CELSIUS, "F", TemperatureUnit.FAHRENHEIT);
        TemperatureUnit unit = aliases.get(text.toUpperCase(Locale.ROOT));
        return unit != null ? unit : parse(TemperatureUnit.class, text);
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String text) {
        String normalized = text.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        try {
            return Enum.valueOf(type, normalized);
        } catch (IllegalArgumentException e) {
            List<String> names = new ArrayList<>();
            for (E constant : type.getEnumConstants()) {
                names.add(constant.name());
            }
            throw new IllegalArgumentException("'" + text + "' is not one of " + names);
        }
    }
}
