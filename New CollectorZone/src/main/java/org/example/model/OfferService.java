package org.example.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.Date;


public class OfferService {

    private final Logger log = LoggerFactory.getLogger(OfferService.class);

   private final ItemService itemService;

   public OfferService (ItemService itemService){
       this.itemService= itemService;
   }

    public Collection<Offer> getAllOffers() {
        List<Offer> offers = new ArrayList<>();
        String sql = "SELECT * FROM offers";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                offers.add(mapRowToOffer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApiException(500, "Database error");
        }
        return offers;
    }

    public Offer getOfferById(UUID id) {
        String sql = "SELECT * FROM offers WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToOffer(rs);
                } else {
                    throw new ApiException(404, "Offer not found");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApiException(500, "Database error");
        }
    }

    public Offer createOffer(Offer offer) {
        String itemId = offer.getItemId();
        if (itemId == null || itemId.isEmpty()) {
            throw new ApiException(400, "Offer must have a valid ID");
        }

        CollectibleItem item = itemService.getItemById(itemId);
        if (item == null) {
            throw new ApiException(404, "Item not found");
        }

        Optional<Offer> lastOfferOpt = getLastOffer(UUID.fromString(itemId));

        double priceToBeat = item.getPrice();
        if (lastOfferOpt.isPresent()) {
            priceToBeat = Math.max(priceToBeat, lastOfferOpt.get().getPrice());
        }

        if (offer.getPrice() <= priceToBeat) {
            log.warn("Bid rejected: {} <= {}", offer.getPrice(), priceToBeat);
            throw new ApiException(409, "Offer must be higher than $" + priceToBeat);
        }

        log.info("Bid ACCEPTED: {} > {}", offer.getPrice(), priceToBeat);

        String sql = "INSERT INTO offers (id, name, email, price, item_id, created_at) VALUES (?, ?, ?, ?, ?, ?)";

        offer.setId(UUID.randomUUID());

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, offer.getId());
            pstmt.setString(2, offer.getName());
            pstmt.setString(3, offer.getEmail());
            pstmt.setDouble(4, offer.getPrice());
            pstmt.setObject(5, UUID.fromString(offer.getItemId()));
            pstmt.setTimestamp(6, new Timestamp(offer.getCreatedAt().getTime()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new ApiException(500, "Could not create offer");
            }

            String message = "¡NEW OFFER! $" + offer.getPrice() + " on " + item.getName();
            BroadcastService.broadcast(message);
            return offer;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApiException(500, "Database error when creating offer");
        }
    }

    public Offer updateOffer(UUID id, Offer offer) {
        String sql = "UPDATE offers SET name = ?, email = ?, price = ? WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, offer.getName());
            pstmt.setString(2, offer.getEmail());
            pstmt.setDouble(3, offer.getPrice());
            pstmt.setObject(4, id);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new ApiException(404, "Offer not found, cannot update");
            }
            offer.setId(id);
            return offer;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApiException(500, "Database error when updating offer");
        }
    }

    public void deleteOffer(UUID id) {
        String sql = "DELETE FROM offers WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, id);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new ApiException(404, "Offer not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApiException(500, "Database error when deleting offer");
        }
    }

    public void offerExists(UUID id) {
        getOfferById(id);
    }

    public Optional<Offer> getLastOffer(UUID itemId) {
        String sql = "SELECT * FROM offers WHERE item_id = ? ORDER BY price DESC LIMIT 1";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, itemId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToOffer(rs));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApiException(500, "Database error when getting last offer");
        }
    }

    private Offer mapRowToOffer(ResultSet rs) throws SQLException {
        Offer offer = new Offer();
        offer.setId(rs.getObject("id", UUID.class));
        offer.setName(rs.getString("name"));
        offer.setEmail(rs.getString("email"));
        offer.setPrice(rs.getDouble("price"));
        offer.setItemId(rs.getObject("item_id", UUID.class).toString());
        offer.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
        return offer;
    }
}

