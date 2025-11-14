package org.example.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.example.model.CollectibleItem;
import org.example.model.ItemService;
import org.example.model.ApiException;
import spark.Request;
import spark.Response;

import java.util.Map;

/**
 * Handles all HTTP API routes related to {@link CollectibleItem} resources.
 * This class uses the SparkJava framework to define RESTful endpoints.
 * It delegates all business logic to the {@link ItemService}.
 */

public class ItemController {

    /**
     * The service layer responsible for handling item-related business logic.
     */
    private final ItemService itemService;

    /**
     * A Gson instance for serializing Java objects to JSON and deserializing JSON to objects.
     */
    private final Gson gson = new Gson();

    /**
     * Constructs a new ItemController with a dependency on an {@link ItemService}.
     *
     * @param itemService The injected service for item business logic.
     */
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /**
     * Handles the HTTP GET request to fetch all collectible items.
     *
     * @param req The Spark HTTP request object.
     * @param res The Spark HTTP response object.
     * @return A JSON string representing a list of all items.
     */
    public String getAllItems(Request req, Response res) {
        res.type("application/json");
        return gson.toJson(itemService.getAllItems());
    }

    /**
     * Handles the HTTP GET request to fetch a single item by its ID.
     * The ID is extracted from the URL path parameter (e.g., /items/:id).
     *
     * @param req The Spark HTTP request object.
     * @param res The Spark HTTP response object.
     * @return A JSON string representing the found item.
     * @throws ApiException if the item is not found (propagated from the service).
     */
    public String getItemById(Request req, Response res) {
        res.type("application/json");
        String id = req.params(":id");

        CollectibleItem item = itemService.getItemById(id);
        return gson.toJson(item);
    }

    /**
     * Handles the HTTP POST request to create a new collectible item.
     * The item's ID is taken from the URL path parameter, and the item's data
     * is taken from the JSON request body.
     *
     * @param req The Spark HTTP request object (contains path param and request body).
     * @param res The Spark HTTP response object.
     * @return A JSON string representing the newly created item. Sets HTTP status 201 (Created).
     * @throws ApiException if the request body is missing or contains invalid JSON data.
     */
    public String createItem(Request req, Response res) {
        res.type("application/json");
        String id = req.params(":id");

        CollectibleItem newItem;
        try {

            newItem = gson.fromJson(req.body(), CollectibleItem.class);
            if (newItem == null) {

                throw new JsonSyntaxException("Empty request body");
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException(400, "Invalid item data format");
        }

        CollectibleItem createdItem = itemService.createItem(id, newItem);
        res.status(201);
        return gson.toJson(createdItem);
    }

    /**
     * Handles the HTTP PUT request to update an existing item by its ID.
     * The item's data for the update is taken from the JSON request body.
     *
     * @param req The Spark HTTP request object (contains path param and request body).
     * @param res The Spark HTTP response object.
     * @return A JSON string representing the updated item.
     * @throws ApiException if the request body is invalid or the item is not found.
     */
    public String updateItem(Request req, Response res) {
        res.type("application/json");
        String id = req.params(":id");

        CollectibleItem itemToUpdate;
        try {
            itemToUpdate = gson.fromJson(req.body(), CollectibleItem.class);
            if (itemToUpdate == null) {
                throw new JsonSyntaxException("Empty request body");
            }
        } catch (Exception e) {
            throw new ApiException(400, "Invalid item data format");
        }

        CollectibleItem updatedItem = itemService.updateItem(id, itemToUpdate);
        return gson.toJson(updatedItem);
    }

    /**
     * Handles the HTTP DELETE request to remove an item by its ID.
     *
     * @param req The Spark HTTP request object (contains path param).
     * @param res The Spark HTTP response object.
     * @return A JSON string with a success message.
     * @throws ApiException if the item is not found.
     */
    public String deleteItem(Request req, Response res) {
        res.type("application/json");
        String id = req.params(":id");

        itemService.deleteItem(id);


        return gson.toJson(Map.of("message", "Item deleted successfully"));
    }

    /**
     * Handles a request to check for the existence of an item.
     * This is often used by clients to see if an ID is available or exists
     * before attempting a more complex operation (like POST or PUT).
     *
     * @param req The Spark HTTP request object (contains path param).
     * @param res The Spark HTTP response object.
     * @return The string "Item exists" with an HTTP 200 (OK) status if found.
     * @throws ApiException if the item is not found (which would result in a 404).
     */
    public String checkItem(Request req, Response res) {
        String id = req.params(":id");

        itemService.itemExists(id);


        res.status(200);
        return "Item exists";
    }
}