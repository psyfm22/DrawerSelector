package com.google.ar.core.examples.java.cloudanchor;

/**
 * Hotspot,
 * Record for hotspots with name and code parameter
 *
 * @param name the name of the hotspot
 * @param code the unique code value
 */
public record Hotspot(String name, long code) {
}
