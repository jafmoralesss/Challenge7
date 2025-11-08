package org.example.Controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.example.Model.ApiException;
import org.example.Model.BroadcastService;
import org.example.Model.Offer;
import org.example.Model.OfferService;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.UUID;


public class OfferController {

    private final Offer offer = new Offer();
    private final OfferService offerService;
    private final Gson gson = new Gson();

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    public String getAllOffers(Request req, Response res) {
        res.type("application/json");
        return gson.toJson(offerService.getAllOffers());
    }

    public String getOfferById(Request req, Response res) {
        res.type("application/json");
        UUID id = UUID.fromString(req.params(":id"));

        Offer offer = offerService.getOfferById(id);
        return gson.toJson(offer);
    }

    public String getLastOffer (Request req, Response res){
        res.type("application/json");
        UUID id = UUID.fromString(req.params(":id"));

        Offer offer = offerService.getLastOffer(id).orElseThrow(() -> new ApiException(404, "Offer not found"));
        return gson.toJson(offer);
    }

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

    public String deleteOffer(Request req, Response res) {
        res.type("application/json");
        UUID id = UUID.fromString(req.params(":id"));

        offerService.deleteOffer(id);


        return gson.toJson(Map.of("message", "Offer deleted successfully"));
    }

    public String checkOffer(Request req, Response res) {
        UUID id = UUID.fromString(req.params(":id"));

        offerService.offerExists(id);


        res.status(200);
        return "Offer exists";
    }
}