package org.example.controller;

import org.example.model.Offer;
import org.example.model.OfferService;
import org.example.model.ApiException;
import spark.Request;
import spark.Response;

import java.util.Date;
import java.util.UUID;

/**
 * Handles HTTP requests related to {@link Offer} objects originating from the
 * HTML web interface (e.g., form submissions).
 * This controller is distinct from {@link OfferController}, which handles the JSON API.
 */
public class OfferWebController {

    private final OfferService offerService;

    public OfferWebController(OfferService offerService) {
        this.offerService = offerService;
    }

    /**
     * Handles the submission of the "make an offer" form (e.g., `POST /offer-web/create`).
     * It reads offer data from the request, attempts to create a new offer,
     * and then redirects back to the main items page (`/items-web`) with a
     * success or error "flash message" stored in the session.
     *
     * @param req The Spark HTTP request object, containing form data as query parameters.
     * @param res The Spark HTTP response object, used for redirection.
     * @return This method always returns null, as the response is finalized by `res.redirect()`.
     */
    public String handleOfferForm(Request req, Response res) {

        try{

            String name = req.queryParams("name");
            String email = req.queryParams("email");
            double price = Double.parseDouble(req.queryParams("itemPrice"));
            String itemId  = req.queryParams("item-id");
            UUID id = UUID.randomUUID();

            Offer newOffer = new Offer(name, email, id, price, itemId, new Date());

            offerService.createOffer(newOffer);

            req.session(true);
            req.session().attribute("successMessage", "Offer submitted successfully.");
            res.redirect("/items-web");


        } catch (ApiException e){
            req.session(true);
            req.session().attribute("errorMessage", e.getMessage());
            res.redirect("/items-web");

        } catch (NumberFormatException e) {
            req.session(true);
            req.session().attribute("errorMessage", "Invalid price format");
            res.redirect("/items-web");
        }

        return null;
    }
}
