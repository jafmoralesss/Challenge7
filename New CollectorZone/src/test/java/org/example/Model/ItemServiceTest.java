package org.example.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is an INTEGRATION TEST.
 * It tests the ItemService class by connecting to the H2 in-memory database.
 * It does NOT use Mockito to mock the service or the DB.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Use this for @BeforeAll if needed
public class ItemServiceTest {

    private final ItemService itemService = new ItemService();

    /**
     * This method runs BEFORE EACH TEST (@Test).
     * It ensures the database is clean and populated with the initial
     * data from 'setup-dev.sql' every time.
     */
    @BeforeEach
    public void setupDatabase() {
        runInitScript();
    }

    /**
     * Tests that the getAllItems() method returns the correct count of
     * items inserted from setup-dev.sql.
     */
    @Test
    public void testGetAllItems_ShouldReturnInitialCount() {

        Collection<CollectibleItem> items = itemService.getAllItems();

        assertNotNull(items, "Item collection should not be null.");

        assertEquals(5, items.size(), "Database should have 5 initial items.");
    }

    /**
     * Tests that we can successfully create a new item.
     */
    @Test
    public void testCreateItem_Success() {

        String id = UUID.randomUUID().toString();
        CollectibleItem newItem = new CollectibleItem(id, "New Test Item", "Description", 99.99);

        itemService.createItem(id, newItem);

        CollectibleItem fetchedItem = itemService.getItemById(id);
        assertNotNull(fetchedItem, "Fetched item should not be null after creation.");
        assertEquals("New Test Item", fetchedItem.getName(), "Item name should match.");
        assertEquals(99.99, fetchedItem.getPrice(), "Item price should match.");
    }

    /**
     * Tests that the system throws an ApiException when we try to
     * fetch an item that does not exist.
     */
    @Test
    public void testGetItemById_NotFound() {
        // Preparation
        String nonExistentId = UUID.randomUUID().toString();

        ApiException exception = assertThrows(ApiException.class, () -> {
            itemService.getItemById(nonExistentId);
        }, "Should throw ApiException for a non-existent ID.");

        assertEquals(404, exception.getStatusCode(), "Status code should be 404.");
        assertEquals("Item not found", exception.getMessage(), "Error message should be 'Item not found'.");
    }

    /**
     * Tests that the system prevents creating an item
     * with a duplicate ID (Primary Key violation).
     */
    @Test
    public void testCreateItem_Fail_DuplicateId() {

        CollectibleItem existingItem = itemService.getAllItems().iterator().next();
        String existingId = existingItem.getId();

        CollectibleItem duplicateItem = new CollectibleItem(existingId, "Duplicate Item", "Desc", 1.0);

        ApiException exception = assertThrows(ApiException.class, () -> {
            itemService.createItem(existingId, duplicateItem);
        }, "Should throw ApiException for a duplicate ID.");

        assertEquals(409, exception.getStatusCode(), "Status code should be 409 (Conflict).");
        assertTrue(exception.getMessage().contains("already exists"), "Error message should indicate a duplicate.");
    }

    /**
     * Reads the 'setup-dev.sql' file from the classpath and executes it
     * against the H2 database to clean and populate it.
     */
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