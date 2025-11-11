package org.example.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OfferServiceTest {

    private final ItemService itemService = new ItemService();
    private final OfferService offerService = new OfferService(itemService);

    private Offer createSampleOffer(String itemId, double price) {
        return new Offer(
                "Test User",
                "test@example.com",
                UUID.randomUUID(),
                price,
                itemId,
                new Date()
        );
    }

    private CollectibleItem getFirstItem() {

        runInitScript();
        return itemService.getAllItems().iterator().next();
    }

    @BeforeEach
    public void setupDatabase() {
        runInitScript();
    }

    /**
     * Happy Path: Test that a valid offer (higher than base price) is created.
     */
    @Test
    public void testCreateOffer_Success() {

        CollectibleItem item = getFirstItem();
        Offer newOffer = createSampleOffer(item.getId(), 1000.00);

        Offer createdOffer = offerService.createOffer(newOffer);

        assertNotNull(createdOffer);
        assertEquals(1000.00, createdOffer.getPrice());
        assertEquals(item.getId(), createdOffer.getItemId());

        Optional<Offer> lastOffer = offerService.getLastOffer(UUID.fromString(item.getId()));
        assertTrue(lastOffer.isPresent());
        assertEquals(1000.00, lastOffer.get().getPrice());
    }

    /**
     * Negative Case: Test that an offer is rejected if it's lower
     * than the item's base price (and there are no other offers).
     */
    @Test
    public void testCreateOffer_Fail_BidTooLow() {

        CollectibleItem item = getFirstItem(); // Price is $621.30
        Offer newOffer = createSampleOffer(item.getId(), 50.00); // $50 is too low

        ApiException exception = assertThrows(ApiException.class, () -> {
            offerService.createOffer(newOffer);
        }, "Should throw ApiException for a bid lower than base price.");

        assertEquals(409, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("$621.3"), "Message should warn about price $621.30");
    }

    /**
     * Negative Case: Test that an offer is rejected if it's not higher
     * than the current highest offer.
     */
    @Test
    public void testCreateOffer_Fail_BidNotHigherThanLastOffer() {

        CollectibleItem item = getFirstItem();
        Offer firstOffer = createSampleOffer(item.getId(), 1000.00);
        offerService.createOffer(firstOffer);

        Offer secondOffer = createSampleOffer(item.getId(), 900.00);

        ApiException exception = assertThrows(ApiException.class, () -> {
            offerService.createOffer(secondOffer);
        }, "Should throw ApiException for a bid lower than the last offer.");

        assertEquals(409, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("$1000.0")); // Should say it must beat $1000
    }

    /**
     * Negative Case: Test that creating an offer for a non-existent item fails.
     */
    @Test
    public void testCreateOffer_Fail_ItemNotFound() {
        // Preparation
        String nonExistentId = UUID.randomUUID().toString();
        Offer newOffer = createSampleOffer(nonExistentId, 5000.00);

        ApiException exception = assertThrows(ApiException.class, () -> {
            offerService.createOffer(newOffer);
        }, "Should throw ApiException for a non-existent item ID.");

        assertEquals(404, exception.getStatusCode());
        assertEquals("Item not found", exception.getMessage());
    }

    /**
     * Tests the getLastOffer() helper method.
     * THIS IS THE *FINAL, FINAL* CORRECTED TEST.
     */
    @Test
    public void testGetLastOffer_Success() {

        CollectibleItem item = getFirstItem();

        offerService.createOffer(createSampleOffer(item.getId(), 700.0));
        offerService.createOffer(createSampleOffer(item.getId(), 900.0));
        offerService.createOffer(createSampleOffer(item.getId(), 1000.0));

        Optional<Offer> lastOffer = offerService.getLastOffer(UUID.fromString(item.getId()));

        assertTrue(lastOffer.isPresent(), "Last offer should be found.");

        assertEquals(1000.0, lastOffer.get().getPrice(), "Last offer should be the highest one ($1000).");
    }

    /**
     * Tests the getLastOffer() helper when no offers exist.
     */
    @Test
    public void testGetLastOffer_Empty() {

        CollectibleItem item = getFirstItem();

        Optional<Offer> lastOffer = offerService.getLastOffer(UUID.fromString(item.getId()));

        assertTrue(lastOffer.isEmpty(), "Last offer optional should be empty.");
    }


    private void runInitScript() {
        String script = "";
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("setup-dev.sql")) {
            if (is == null) {
                throw new RuntimeException("Could not find setup-dev.sql in classpath");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                script = reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading setup-dev.sql", e);
        }

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(script);
        } catch (SQLException e) {
            throw new RuntimeException("Error executing setup-dev.sql", e);
        }
    }
}