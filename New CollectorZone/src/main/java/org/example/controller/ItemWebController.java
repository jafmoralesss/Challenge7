package org.example.controller;

import org.example.model.*;
import org.example.dto.ItemWebResponse;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Session;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles all HTTP requests for the user-facing, HTML-based web interface.
 * This controller is distinct from {@link ItemController}, which handles the JSON API.
 * This class is responsible for rendering {@link ModelAndView} objects
 * using a {@link MustacheTemplateEngine} to produce HTML pages.
 */
public class ItemWebController {

    /**
     * The service layer for item-related business logic, offer-related business logic
     * and template engine used to render .mustache files into HTML.
     */
    private final ItemService itemService;
    private final OfferService offerService;
    private final MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();

    /**
     * Constructs a new ItemWebController with its required service dependencies.
     *
     * @param itemService  The service for managing items.
     * @param offerService The service for managing offers.
     */
    public ItemWebController(ItemService itemService, OfferService offerService) {
        this.itemService = itemService;
        this.offerService = offerService;
    }

    /**
     * Handles the `GET /items-web` request.
     * Renders the main item browsing page, applying any filters and displaying
     * session-based notifications (flash messages).
     *
     * @param req The Spark HTTP request object.
     * @param res The Spark HTTP response object.
     * @return A string containing the rendered HTML for the `items.mustache` page.
     */
    public String showItemsPage(Request req, Response res) {

        Map<String, Object> model = new HashMap<>();

        Session session = req.session(false);
        if (session != null) {

            String success = session.attribute("successMessage");
            if (success != null) {
                model.put("success", success);
                session.removeAttribute("successMessage");
            }

            String error = session.attribute("errorMessage");
            if (error != null) {
                model.put("error", error);
                session.removeAttribute("errorMessage");
            }
        }

        String search = req.queryParams("search");
        String minPriceStr = req.queryParams("minPrice");
        String maxPriceStr = req.queryParams("maxPrice");

        Collection<CollectibleItem> allItems = itemService.getAllItems();

        List<ItemWebResponse> itemsWeb = allItems.stream().map(i -> {
            ItemWebResponse itemWeb = new ItemWebResponse();
            itemWeb.setId(i.getId());
            itemWeb.setName(i.getName());
            itemWeb.setDescription(i.getDescription());
            itemWeb.setPrice(i.getPrice());
            Optional<Offer> lastOffer = offerService.getLastOffer(UUID.fromString(i.getId()));
            lastOffer.ifPresent(offer -> itemWeb.setLastOffer(offer.getPrice()));
            return itemWeb;
        }).collect(Collectors.toList());

        if (search != null && !search.isEmpty()) {
            String finalSearch = search.toLowerCase();
            itemsWeb = itemsWeb.stream()
                    .filter(item ->
                            item.getName().toLowerCase().contains(finalSearch) ||
                                    item.getDescription().toLowerCase().contains(finalSearch)
                    )
                    .collect(Collectors.toList());
        }

        if (minPriceStr != null && !minPriceStr.isEmpty()) {
            try {
                double minPrice = Double.parseDouble(minPriceStr);
                itemsWeb = itemsWeb.stream()
                        .filter(item -> {
                            double displayPrice = Math.max(item.getPrice(), item.getLastOffer());
                            return displayPrice >= minPrice;
                        })
                        .collect(Collectors.toList());
            } catch (NumberFormatException e) { }
        }

        if (maxPriceStr != null && !maxPriceStr.isEmpty()) {
            try {
                double maxPrice = Double.parseDouble(maxPriceStr);
                itemsWeb = itemsWeb.stream()
                        .filter(item -> {
                            double displayPrice = Math.max(item.getPrice(), item.getLastOffer());
                            return displayPrice <= maxPrice;
                        })
                        .collect(Collectors.toList());
            } catch (NumberFormatException e) { }
        }


        model.put("items", itemsWeb);

        model.put("search", search);
        model.put("minPrice", minPriceStr);
        model.put("maxPrice", maxPriceStr);

        ModelAndView mav = new ModelAndView(model, "items.mustache");
        return templateEngine.render(mav);
    }

    /**
     * Handles the submission of the "add new item" form (e.g., `POST /items-web/create`).
     * It reads form data from the request, attempts to create a new item,
     * and then redirects back to the main items page with a success or error message.
     *
     * @param req The Spark HTTP request object, containing form data as query parameters.
     * @param res The Spark HTTP response object, used for redirection.
     * @return This method always returns null, as the response is finalized by `res.redirect()`.
     */
    public String handleItemForm(Request req, Response res) {

        try {
            String name = req.queryParams("itemName");
            String description = req.queryParams("itemDescription");
            String imageUrl = req.queryParams("itemImageUrl");
            double price = Double.parseDouble(req.queryParams("itemPrice"));

            String id = UUID.randomUUID().toString();
            CollectibleItem newItem = new CollectibleItem(id, name, description, price);

            itemService.createItem(id, newItem);

            req.session(true);
            req.session().attribute("successMessage", "New item '" + name + "' added successfully!");
            res.redirect("/items-web");

        } catch (ApiException e) {
            req.session(true);
            req.session().attribute("errorMessage", e.getMessage());
            res.redirect("/items-web");

        } catch (NumberFormatException e) {
            req.session(true);
            req.session().attribute("errorMessage", "Invalid price format. Please enter a valid number.");
            res.redirect("/items-web");
        }
        return null;
    }
}
