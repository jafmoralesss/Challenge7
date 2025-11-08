package org.example.Controller;

import com.sun.tools.attach.AttachOperationFailedException;
import org.example.Model.ItemService;
import org.example.Model.Offer;
import org.example.Model.OfferService;
import org.example.Model.ApiException;
import spark.Request;
import spark.Response;

import java.util.Date;
import java.util.UUID;

public class OfferWebController {

    private final OfferService offerService;

    public OfferWebController(OfferService offerService) {
        this.offerService = offerService;
    }

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
