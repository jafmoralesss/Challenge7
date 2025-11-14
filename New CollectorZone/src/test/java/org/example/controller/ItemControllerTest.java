package org.example.controller;

import com.google.gson.Gson;
import org.example.model.CollectibleItem;
import org.example.model.ItemService;
import org.example.model.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import spark.Request;
import spark.Response;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @Mock
    private Request req;

    @Mock
    private Response res;

    @InjectMocks
    private ItemController itemController;

    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllItems() {
        CollectibleItem item = new CollectibleItem("1", "Test Item", "Description", 100.0);
        List<CollectibleItem> items = Collections.singletonList(item);
        when(itemService.getAllItems()).thenReturn(items);

        String result = itemController.getAllItems(req, res);

        verify(res).type("application/json");
        assertEquals(gson.toJson(items), result);
    }

    @Test
    void getItemById() {
        String id = "1";
        CollectibleItem item = new CollectibleItem(id, "Test Item", "Description", 100.0);
        when(req.params(":id")).thenReturn(id);
        when(itemService.getItemById(id)).thenReturn(item);

        String result = itemController.getItemById(req, res);

        verify(res).type("application/json");
        assertEquals(gson.toJson(item), result);
    }

    @Test
    void createItem() {
        String id = "1";
        CollectibleItem item = new CollectibleItem(id, "Test Item", "Description", 100.0);
        String itemJson = gson.toJson(item);

        when(req.params(":id")).thenReturn(id);
        when(req.body()).thenReturn(itemJson);
        when(itemService.createItem(eq(id), argThat(i ->
                Objects.equals(i.getId(), item.getId()) &&
                Objects.equals(i.getName(), item.getName()) &&
                Objects.equals(i.getDescription(), item.getDescription()) &&
                i.getPrice() == item.getPrice()
        ))).thenReturn(item);

        String result = itemController.createItem(req, res);

        verify(res).type("application/json");
        verify(res).status(201);
        assertEquals(gson.toJson(item), result);
    }

    @Test
    void createItem_invalidJson() {
        when(req.params(":id")).thenReturn("1");
        when(req.body()).thenReturn("invalid json");

        ApiException exception = assertThrows(ApiException.class, () -> itemController.createItem(req, res));

        assertEquals(400, exception.getStatusCode());
        assertEquals("Invalid item data format", exception.getMessage());
    }
    
    @Test
    void createItem_emptyBody() {
        when(req.params(":id")).thenReturn("1");
        when(req.body()).thenReturn("");

        ApiException exception = assertThrows(ApiException.class, () -> itemController.createItem(req, res));

        assertEquals(400, exception.getStatusCode());
        assertEquals("Invalid item data format", exception.getMessage());
    }

    @Test
    void updateItem() {
        String id = "1";
        CollectibleItem item = new CollectibleItem(id, "Updated Item", "Updated Description", 150.0);
        String itemJson = gson.toJson(item);

        when(req.params(":id")).thenReturn(id);
        when(req.body()).thenReturn(itemJson);
        when(itemService.updateItem(eq(id), argThat(i ->
                Objects.equals(i.getId(), item.getId()) &&
                Objects.equals(i.getName(), item.getName()) &&
                Objects.equals(i.getDescription(), item.getDescription()) &&
                i.getPrice() == item.getPrice()
        ))).thenReturn(item);

        String result = itemController.updateItem(req, res);

        verify(res).type("application/json");
        assertEquals(gson.toJson(item), result);
    }

    @Test
    void updateItem_invalidJson() {
        when(req.params(":id")).thenReturn("1");
        when(req.body()).thenReturn("invalid json");

        ApiException exception = assertThrows(ApiException.class, () -> itemController.updateItem(req, res));

        assertEquals(400, exception.getStatusCode());
        assertEquals("Invalid item data format", exception.getMessage());
    }
    
    @Test
    void updateItem_emptyBody() {
        when(req.params(":id")).thenReturn("1");
        when(req.body()).thenReturn("");

        ApiException exception = assertThrows(ApiException.class, () -> itemController.updateItem(req, res));

        assertEquals(400, exception.getStatusCode());
        assertEquals("Invalid item data format", exception.getMessage());
    }

    @Test
    void deleteItem() {
        String id = "1";
        when(req.params(":id")).thenReturn(id);

        String result = itemController.deleteItem(req, res);

        verify(itemService).deleteItem(id);
        verify(res).type("application/json");
        assertEquals(gson.toJson(Map.of("message", "Item deleted successfully")), result);
    }

    @Test
    void checkItem() {
        String id = "1";
        when(req.params(":id")).thenReturn(id);

        String result = itemController.checkItem(req, res);

        verify(itemService).itemExists(id);
        verify(res).status(200);
        assertEquals("Item exists", result);
    }
}