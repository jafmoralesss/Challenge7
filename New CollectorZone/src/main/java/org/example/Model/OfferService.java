package org.example.Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class OfferService {

    private static final Map<UUID, Offer> offerDatabase = new HashMap<>();
    private final Logger log = LoggerFactory.getLogger(OfferService.class);

   private final ItemService itemService;

   public OfferService (ItemService itemService){
       this.itemService= itemService;
   }
    public Collection<Offer> getAllOffers() {
        return offerDatabase.values();
    }

    public Offer getOfferById(UUID id) {
        Offer offer = offerDatabase.get(id);
        if (offer == null) {

            throw new ApiException(404, "Offer not found");
        }
        return offer;
    }

    public Offer createOffer(Offer offer) {

       String itemId = offer.getItemId();
       if(itemId == null || itemId.isEmpty()){
           throw new ApiException(400, "Offer must have a valid ID");
       }

       CollectibleItem item = itemService.getItemById(itemId);
       if (item == null){
           throw new ApiException(404, "Item not found");
       }

       Optional <Offer> lastOfferOpt = getLastOffer(UUID.fromString(itemId));

       double priceToBeat= item.getPrice();
       if (lastOfferOpt.isPresent()){
           priceToBeat = Math.max(priceToBeat, lastOfferOpt.get().getPrice());
       }

       if (offer.getPrice() <= priceToBeat) {
           log.warn("Bid rejected: {} <= {}", offer.getPrice(), priceToBeat);
           throw new ApiException(409, "Offer must be higher than $" + priceToBeat);
       }

        log.info("Bid ACCEPTED: {} > {}", offer.getPrice(), priceToBeat);
        offer.setId(UUID.randomUUID());
        offerDatabase.put(offer.getId(), offer);
        String message = "¡NEW OFFER! $" + offer.getPrice() + " on " + item.getName();
        BroadcastService.broadcast(message);
        return offer;
    }

    public Offer updateOffer(UUID id, Offer offer) {
        if (!offerDatabase.containsKey(id)) {

            throw new ApiException(404, "Offer not found, cannot update");
        }
        offer.setId(id);
        offerDatabase.put(id, offer);
        return offer;
    }

    public void deleteOffer(UUID id) {
        if (offerDatabase.remove(id) == null) {

            throw new ApiException(404, "Offer not found");
        }

    }

    public void offerExists(UUID id) {
        if (!offerDatabase.containsKey(id)) {

            throw new ApiException(404, "Offer not found");
        }

    }

    public Optional<Offer> getLastOffer (UUID itemId){
       return offerDatabase.values().stream()
                .filter(o -> UUID.fromString(o.getItemId()).equals(itemId))
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .findFirst();
    }

}
