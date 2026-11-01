package com.smarthome.prototype;

/**
 * Prototype pattern: an object able to produce an independent copy of itself.
 * Changing the copy must never modify the original.
 */
public interface Prototype<T> {

    T copy();
}
