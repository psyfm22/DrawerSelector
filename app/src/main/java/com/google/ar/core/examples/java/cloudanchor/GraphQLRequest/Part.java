package com.google.ar.core.examples.java.cloudanchor.GraphQLRequest;


public class Part {
    private String id;
    private String name;
    private String mpn;
    private MedianPrice medianPrice1000;
    private Category category;
    private Manufacturer manufacturer;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMpn() {
        return mpn;
    }

    public void setMpn(String mpn) {
        this.mpn = mpn;
    }

    public MedianPrice getMedianPrice1000() {
        return medianPrice1000;
    }

    public void setMedianPrice1000(MedianPrice medianPrice1000) {
        this.medianPrice1000 = medianPrice1000;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
    }
}
