package org.example.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.example.model.ApiException;
import org.example.model.Offer;
import org.example.model.OfferService;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.UUID;

/**
 * Handles all HTTP API routes related to {@link Offer} resources.
 * This class uses the SparkJava framework to define RESTful endpoints
 * for creating, reading, updating, and deleting offers.
 * It delegates all business logic to the {@link OfferService}.
 */
public class OfferController {

    private final Offer offer = new Offer();
    private final OfferService offerService;
    private final Gson gson = new Gson();

    /**
     * Constructs a new OfferController with a dependency on an {@link OfferService}.
     *
     * @param offerService The injected service for offer business logic.
     */
    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    /**
     * Handles the HTTP GET request to fetch all offers.
     *
     * @param req The Spark HTTP request object.
     * @param res The Spark HTTP response object.
     * @return A JSON string representing a list of all offers.
     */
    public String getAllOffers(Request req, Response res) {
        res.type("application/json");
        return gson.toJson(offerService.getAllOffers());
    }

    /**
     * Handles the HTTP GET request to fetch a single offer by its unique ID.
     * The ID is extracted from the URL path parameter.
     *
     * @param req The Spark HTTP request object.
     * @param res The Spark HTTP response object.
     * @return A JSON string representing the found offer.
     * @throws ApiException if the offer is not found.
     */
    public String getOfferById(Request req, Response res) {
        res.type("application/json");
        UUID id = UUID.fromString(req.params(":id"));

        Offer offer = offerService.getOfferById(id);
        return gson.toJson(offer);
    }

    /**
     * Handles the HTTP GET request to fetch the last (highest) offer for a specific item.
     * The item's ID is extracted from the URL path parameter.
     *
     * @param req The Spark HTTP request object.
     * @param res The Spark HTTP response object.
     * @return A JSON string representing the last offer for the specified item.
     * @throws ApiException if no offer is found for the item (HTTP 404).
     */
    public String getLastOffer (Request req, Response res){
        res.type("application/json");
        UUID id = UUID.fromString(req.params(":id"));

        Offer offer = offerService.getLastOffer(id).orElseThrow(() -> new ApiException(404, "Offer not found"));
        return gson.toJson(offer);
    }

    /**
     * Handles the HTTP POST request to create a new offer for a specific item.
     * The item's ID is taken from the URL path parameter, and the offer's data
     * (e.g., price, user) is taken from the JSON request body.
     *
     * @param req The Spark HTTP request object (contains path param and request body).
     * @param res The Spark HTTP response object.
     * @return A JSON string representing the newly created offer. Sets HTTP status 201 (Created).
     * @throws ApiException if the request body is missing or contains invalid JSON data.
     */
    public String createOffer(Request req, Response res) {
        res.type("application/json");
        String itemId = req.params(":id");

        Offer newOffer;
        try {

            newOffer = gson.fromJson(req.body(), Offer.class);
            if (newOffer == null) {

                throw new JsonSyntaxException("Empty request body");
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException(400, "Invalid offer data format");
        }

        newOffer.setItemId(itemId);

        Offer createdOffer = offerService.createOffer(newOffer);
        res.status(201);
        return gson.toJson(createdOffer);
    }

    /**
     * Handles the HTTP PUT request to update an existing offer by its ID.
     * The offer's data for the update is taken from the JSON request body.
     *
     * @param req The Spark HTTP request object (contains path param and request body).
     * @param res The Spark HTTP response object.
     * @return A JSON string representing the updated offer.
     * @throws ApiException if the request body is invalid or the offer is not found.
     */
    public String updateOffer(Request req, Response res) {
        res.type("application/json");
        UUID id = UUID.fromString(req.params(":id"));

        Offer offerToUpdate;
        try {
            offerToUpdate = gson.fromJson(req.body(), Offer.class);
            if (offerToUpdate == null) {
                throw new JsonSyntaxException("Empty request body");
            }
        } catch (Exception e) {
            throw new ApiException(400, "Invalid offer data format");
        }

        Offer updatedOffer = offerService.updateOffer(id, offerToUpdate);
        return gson.toJson(updatedOffer);
    }

    /**
     * Handles the HTTP DELETE request to remove an offer by its ID.
     *
     * @param req The Spark HTTP request object (contains path param).
     * @param res The Spark HTTP response object.
     * @return A JSON string with a success message.
     * @throws ApiException if the offer is not found.
     */
    public String deleteOffer(Request req, Response res) {
        res.type("application/json");
        UUID id = UUID.fromString(req.params(":id"));

        offerService.deleteOffer(id);


        return gson.toJson(Map.of("message", "Offer deleted successfully"));
    }

    /**
     * Handles a request to check for the existence of an offer by its ID.
     *
     * @param req The Spark HTTP request object (contains path param).
     * @param res The Spark HTTP response object.
     * @return The string "Offer exists" with an HTTP 200 (OK) status if found.
     * @throws ApiException if the offer is not found (which would result in a 404).
     */
    public String checkOffer(Request req, Response res) {
        UUID id = UUID.fromString(req.params(":id"));

        offerService.offerExists(id);


        res.status(200);
        return "Offer exists";
    }
}