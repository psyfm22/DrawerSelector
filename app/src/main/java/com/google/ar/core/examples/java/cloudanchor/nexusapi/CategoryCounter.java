package com.google.ar.core.examples.java.cloudanchor.nexusapi;

public class CategoryCounter {
    private String id;

    private String name;
    private int counter;

    public CategoryCounter(String id, String name){
        this.id = id;
        this.name = name;
        this.counter = 0;
    }

    public void addCounter(){
        counter++;
    }
    public int getCounter() {
        return counter;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
}