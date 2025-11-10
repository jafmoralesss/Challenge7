package org.example;

import com.google.gson.Gson;
import org.example.Controller.ItemController;
import org.example.Controller.ItemWebController;
import org.example.Controller.OfferController;
import org.example.Controller.OfferWebController;
import org.example.Model.ApiError;
import org.example.Model.ApiException;
import org.example.Model.ItemService;
import org.example.Model.OfferService;
import org.example.Model.Database;
import java.sql.Connection;
import java.sql.Statement;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class ApiService {

    public static void runInitScript() {
        String script = "";
        try (InputStream is = ApiService.class.getClassLoader().getResourceAsStream("setup-dev.sql")) {
            if (is == null) {
                System.err.println("setup-dev.sql not found.");
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                script = reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            System.out.println("Executing setup-dev.sql...");
            stmt.execute(script);
            System.out.println("Script completed.");
        } catch (Exception e) {
            System.err.println("Error when executing setup-dev.sql:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        if (System.getenv("APP_ENV") == null || !System.getenv("APP_ENV").equals("prod")) {
            runInitScript();
        }

        ItemService itemService = new ItemService();
        OfferService offerService = new OfferService(itemService);

        Gson gson = new Gson();

        ItemController itemController = new ItemController(itemService);
        OfferController offerController = new OfferController(offerService);

        ItemWebController itemWebController = new ItemWebController(itemService, offerService);
        OfferWebController offerWebController = new OfferWebController(offerService);

        port(4567);
        staticFiles.location("/public");

        webSocket("/notifications", WebSocketHandler.class);

        exception(ApiException.class, (exception, req, res) -> {
            res.status(exception.getStatusCode());
            res.type("application/json");
            res.body(gson.toJson(new ApiError(exception.getMessage())));
        });

        exception(Exception.class, (exception, req, res) -> {
            exception.printStackTrace();
            res.status(500);
            res.type("application/json");
            res.body(gson.toJson(new ApiError("An unexpected internal server error occurred.")));
        });

        get("/items", itemController::getAllItems);
        get("/items/:id", itemController::getItemById);
        post("/items/:id", itemController::createItem);
        put("/items/:id", itemController::updateItem);
        delete("/items/:id", itemController::deleteItem);
        options("/items/:id", itemController::checkItem);

        get("/offers", offerController::getAllOffers);
        get("/offers/:id", offerController::getOfferById);
        get("/offers/:id/lastest", offerController::getLastOffer);
        post("/offers/:id", offerController::createOffer);
        put("/offers/:id", offerController::updateOffer);
        delete("/offers/:id", offerController::deleteOffer);
        options("/offers/:id", offerController::checkOffer);

        get("/items-web", itemWebController::showItemsPage);
        post("/items-web", itemWebController::handleItemForm);

        post("/offers-web", offerWebController::handleOfferForm);

    }
}