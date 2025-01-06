package com.google.ar.core.examples.java.cloudanchor;

public class Hotspot {
    private String name;
    private long code;


    public Hotspot(String name, long code) {
        this.name = name;
        this.code = code;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }
}
