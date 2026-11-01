package com.smarthome.factory;

import com.smarthome.abstractfactory.SmartHomeFactory;
import com.smarthome.model.Device;
import com.smarthome.model.enums.DeviceType;
import com.smarthome.util.IdGenerator;

/**
 * Factory Method: each concrete creator decides which concrete {@link Device} to build,
 * so clients only ever work with the {@code Device} abstraction and a {@link DeviceType}.
 * <p>
 * For device kinds that exist in every ecosystem (light, thermostat, camera) the creator delegates
 * to the home's {@link SmartHomeFactory}, which picks the vendor-specific product.
 */
public abstract class DeviceCreator {

    public final Device create(String name, SmartHomeFactory family) {
        return createDevice(IdGenerator.next("dev"), name, family);
    }

    /** The factory method. */
    protected abstract Device createDevice(String id, String name, SmartHomeFactory family);

    public static DeviceCreator forType(DeviceType type) {
        if (type == null) {
            throw new IllegalArgumentException("Device type is required");
        }
        return switch (type) {
            case LIGHT -> new LightCreator();
            case THERMOSTAT -> new ThermostatCreator();
            case CAMERA -> new CameraCreator();
            case FAN -> new FanCreator();
            case TV -> new TVCreator();
            case DOOR_LOCK -> new DoorLockCreator();
        };
    }
}
