package org.example.Controller;

import org.example.Model.CollectibleItem;
import org.example.Model.ItemService;
import org.example.Model.Offer;
import org.example.Model.OfferService;
import org.example.dto.ItemWebResponse;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Session;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.*;
import java.util.stream.Collectors;

public class ItemWebController {

    private final ItemService itemService;
    private final OfferService offerService;
    private final MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();

    public ItemWebController(ItemService itemService, OfferService offerService) {
        this.itemService = itemService;
        this.offerService = offerService;
    }

    /**
     * Handles GET /items-web
     * Muestra la página principal con filtros, notificaciones y la lista de artículos.
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
     * Handles POST /items-web
     * Procesa el formulario "Add New Item".
     */
    public String handleItemForm(Request req, Response res) {
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
        return null;
    }
}
