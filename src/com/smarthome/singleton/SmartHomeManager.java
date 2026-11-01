package com.smarthome.singleton;

import com.smarthome.abstractfactory.SmartHomeFactory;
import com.smarthome.model.HomeConfiguration;
import com.smarthome.model.SmartHome;
import com.smarthome.model.enums.Ecosystem;
import com.smarthome.util.IdGenerator;

/**
 * Singleton: the one place holding the global smart-home state.
 * <p>
 * If several managers existed, the CLI, the REST API and the scheduler could each see a different
 * house, and a scenario executed through one would not change the devices seen by the others.
 */
public final class SmartHomeManager {

    private static volatile SmartHomeManager instance;

    private SmartHome home;

    private SmartHomeManager() {
        home = new SmartHome(IdGenerator.next("home"), "My Smart Home", Ecosystem.GENERIC);
    }

    public static SmartHomeManager getInstance() {
        if (instance == null) {
            synchronized (SmartHomeManager.class) {
                if (instance == null) {
                    instance = new SmartHomeManager();
                }
            }
        }
        return instance;
    }

    public synchronized SmartHome getHome() {
        return home;
    }

    /** Replaces the current home with a new, empty one. */
    public synchronized SmartHome createHome(String name, Ecosystem ecosystem) {
        home = new SmartHome(IdGenerator.next("home"), name, ecosystem);
        return home;
    }

    public HomeConfiguration getConfiguration() {
        return getHome().getConfiguration();
    }

    /** Device family matching the ecosystem the user chose for the home. */
    public SmartHomeFactory getDeviceFamily() {
        return SmartHomeFactory.forEcosystem(getHome().getEcosystem());
    }
}
