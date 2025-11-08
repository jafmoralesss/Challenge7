package org.example.Controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.example.Model.CollectibleItem;
import org.example.Model.ItemService;
import org.example.Model.ApiException;
import spark.Request;
import spark.Response;

import java.util.Map;


public class ItemController {

    private final ItemService itemService;
    private final Gson gson = new Gson();

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    public String getAllItems(Request req, Response res) {
        res.type("application/json");
        return gson.toJson(itemService.getAllItems());
    }

    public String getItemById(Request req, Response res) {
        res.type("application/json");
        String id = req.params(":id");

        CollectibleItem item = itemService.getItemById(id);
        return gson.toJson(item);
    }

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

    public String deleteItem(Request req, Response res) {
        res.type("application/json");
        String id = req.params(":id");

        itemService.deleteItem(id);


        return gson.toJson(Map.of("message", "Item deleted successfully"));
    }

    public String checkItem(Request req, Response res) {
        String id = req.params(":id");

        itemService.itemExists(id);


        res.status(200);
        return "Item exists";
    }
}