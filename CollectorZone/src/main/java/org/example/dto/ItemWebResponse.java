package org.example.dto;

public class ItemWebResponse {
    private String id;
    private String name;
    private String description;
    private double price;
    private double lastOffer;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getLastOffer() {
        return lastOffer;
    }

    public void setLastOffer(double lastOffer) {
        this.lastOffer = lastOffer;
    }
}
