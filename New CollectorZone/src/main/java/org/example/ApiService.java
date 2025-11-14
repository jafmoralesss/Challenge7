package org.example;

import com.google.gson.Gson;
import org.example.controller.ItemController;
import org.example.controller.ItemWebController;
import org.example.controller.OfferController;
import org.example.controller.OfferWebController;
import org.example.model.ApiError;
import org.example.model.ApiException;
import org.example.model.ItemService;
import org.example.model.OfferService;
import org.example.model.Database;
import java.sql.Connection;
import java.sql.Statement;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class ApiService {

    /**
     * Reads and executes the `setup-dev.sql` script from the project's resources.
     * This method is intended for development environments to initialize or reset
     * the database schema and any seed data.
     * <p>
     * It prints status messages to standard out and errors to standard err.
     */
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

    /**
     * The main application entry point.
     * This method starts the SparkJava server, configures it,
     * and defines all application routes.
     *
     *
     * @param args Command line arguments (not used by this application).
     */
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