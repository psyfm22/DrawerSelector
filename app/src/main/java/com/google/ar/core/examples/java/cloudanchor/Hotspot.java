package com.google.ar.core.examples.java.cloudanchor;

public class Hotspot {
    private long code;
    private String name;
    private String category;
    private int currentStorage;
    private int maxStorage;

    // Default constructor for Firebase deserialization
    public Hotspot() {
    }

    public Hotspot(long code, String name, String category, int currentStorage, int maxStorage) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.currentStorage = currentStorage;
        this.maxStorage = maxStorage;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getCurrentStorage() {
        return currentStorage;
    }

    public void setCurrentStorage(int currentStorage) {
        this.currentStorage = currentStorage;
    }

    public int getMaxStorage() {
        return maxStorage;
    }

    public void setMaxStorage(int maxStorage) {
        this.maxStorage = maxStorage;
    }
}