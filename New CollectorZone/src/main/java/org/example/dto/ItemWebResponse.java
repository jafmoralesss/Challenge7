package org.example.dto;


/**
 * This is a data transfer object for an item sent as a web response.
 * Used to send item details to the client after a request.
 */
public class ItemWebResponse {

    /**
     * Item details, including id, name, description, price and last offer.
     */
    private String id;
    private String name;
    private String description;
    private double price;
    private double lastOffer;

    /**
     * Gets the unique identifier of the item.
     *
     * @return The item's ID string.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the item.
     *
     * @param id The string to be set as the item's ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the name of the item.
     *
     * @return The item's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the item.
     *
     * @param name The string to be set as the item's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of the item.
     *
     * @return The item's description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the item.
     *
     * @param description The string to be set as the item's description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the price of the item.
     *
     * @return The item's price.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets the price of the item.
     *
     * @param price The double value to be set as the item's price.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Gets the last offer made for the item.
     *
     * @return The last offer price.
     */
    public double getLastOffer() {
        return lastOffer;
    }

    /**
     * Sets the last offer made for the item.
     *
     * @param lastOffer The double value to be set as the last offer.
     */
    public void setLastOffer(double lastOffer) {
        this.lastOffer = lastOffer;
    }
}
