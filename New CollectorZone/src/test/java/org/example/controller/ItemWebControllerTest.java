package org.example.controller;

import org.example.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import spark.Request;
import spark.Response;
import spark.Session;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemWebControllerTest {

    private ItemService itemService;
    private OfferService offerService;
    private ItemWebController itemWebController;
    private Request request;
    private Response response;
    private Session session;

    @BeforeEach
    void setUp() {
        itemService = mock(ItemService.class);
        offerService = mock(OfferService.class);
        itemWebController = new ItemWebController(itemService, offerService);
        request = mock(Request.class);
        response = mock(Response.class);
        session = mock(Session.class);

        when(request.session()).thenReturn(session);
        when(request.session(anyBoolean())).thenReturn(session);
    }

    @Test
    void showItemsPage() {
        when(itemService.getAllItems()).thenReturn(Collections.emptyList());
        String result = itemWebController.showItemsPage(request, response);
        assertTrue(result.contains("items"));
    }

    @Test
    void handleItemFormSuccess() {
        when(request.queryParams("itemName")).thenReturn("Test Item");
        when(request.queryParams("itemDescription")).thenReturn("Test Description");
        when(request.queryParams("itemImageUrl")).thenReturn("http://example.com/image.jpg");
        when(request.queryParams("itemPrice")).thenReturn("100.0");

        itemWebController.handleItemForm(request, response);

        ArgumentCaptor<CollectibleItem> itemCaptor = ArgumentCaptor.forClass(CollectibleItem.class);
        verify(itemService).createItem(anyString(), itemCaptor.capture());
        assertEquals("Test Item", itemCaptor.getValue().getName());

        verify(session).attribute("successMessage", "New item 'Test Item' added successfully!");
        verify(response).redirect("/items-web");
    }

    @Test
    void handleItemFormApiException() {
        when(request.queryParams("itemName")).thenReturn("Test Item");
        when(request.queryParams("itemDescription")).thenReturn("Test Description");
        when(request.queryParams("itemImageUrl")).thenReturn("http://example.com/image.jpg");
        when(request.queryParams("itemPrice")).thenReturn("100.0");

        doThrow(new ApiException(500, "Test Exception")).when(itemService).createItem(anyString(), any(CollectibleItem.class));

        itemWebController.handleItemForm(request, response);

        verify(session).attribute("errorMessage", "Test Exception");
        verify(response).redirect("/items-web");
    }

    @Test
    void handleItemFormNumberFormatException() {
        when(request.queryParams("itemName")).thenReturn("Test Item");
        when(request.queryParams("itemDescription")).thenReturn("Test Description");
        when(request.queryParams("itemImageUrl")).thenReturn("http://example.com/image.jpg");
        when(request.queryParams("itemPrice")).thenReturn("invalid-price");

        itemWebController.handleItemForm(request, response);

        verify(session).attribute("errorMessage", "Invalid price format. Please enter a valid number.");
        verify(response).redirect("/items-web");
    }

    @Test
    void showItemsPageWithFilters() {
        String id = UUID.randomUUID().toString();
        CollectibleItem item = new CollectibleItem(id, "Test Item", "Test Description", 100.0);
        when(itemService.getAllItems()).thenReturn(Collections.singletonList(item));
        when(request.queryParams("search")).thenReturn("Test");
        when(request.queryParams("minPrice")).thenReturn("50");
        when(request.queryParams("maxPrice")).thenReturn("150");

        String result = itemWebController.showItemsPage(request, response);

        assertTrue(result.contains("Test Item"));
    }

    @Test
    void showItemsPageWithSessionMessages() {
        when(request.session(false)).thenReturn(session);
        when(itemService.getAllItems()).thenReturn(Collections.emptyList());
        when(session.attribute("successMessage")).thenReturn("Success!");
        when(session.attribute("errorMessage")).thenReturn("Error!");

        String result = itemWebController.showItemsPage(request, response);

        assertTrue(result.contains("Success!"));
        assertTrue(result.contains("Error!"));
        verify(session).removeAttribute("successMessage");
        verify(session).removeAttribute("errorMessage");
    }

    @Test
    void showItemsPageWithOffer() {
        String id = UUID.randomUUID().toString();
        CollectibleItem item = new CollectibleItem(id, "Test Item", "Test Description", 100.0);
        Offer offer = new Offer("testuser", "user@test.com", UUID.randomUUID(), 120.0, id, new Date());
        when(itemService.getAllItems()).thenReturn(Collections.singletonList(item));
        when(offerService.getLastOffer(UUID.fromString(id))).thenReturn(Optional.of(offer));

        String result = itemWebController.showItemsPage(request, response);

        assertTrue(result.contains("120.0"));
    }

    @Test
    void showItemsPageWithNoMatchingFilters() {
        String id = UUID.randomUUID().toString();
        CollectibleItem item = new CollectibleItem(id, "Test Item", "Test Description", 100.0);
        when(itemService.getAllItems()).thenReturn(Collections.singletonList(item));
        when(request.queryParams("search")).thenReturn("NoMatch");

        String result = itemWebController.showItemsPage(request, response);

        assertFalse(result.contains("Test Item"));
    }

    @Test
    void showItemsPageWithInvalidPriceFilters() {
        String id = UUID.randomUUID().toString();
        CollectibleItem item = new CollectibleItem(id, "Test Item", "Test Description", 100.0);
        when(itemService.getAllItems()).thenReturn(Collections.singletonList(item));
        when(request.queryParams("minPrice")).thenReturn("invalid");
        when(request.queryParams("maxPrice")).thenReturn("invalid");

        String result = itemWebController.showItemsPage(request, response);

        assertTrue(result.contains("Test Item"));
    }

    @Test
    void showItemsPageWithNullSession() {
        when(request.session(false)).thenReturn(null);
        when(itemService.getAllItems()).thenReturn(Collections.emptyList());
        String result = itemWebController.showItemsPage(request, response);
        assertTrue(result.contains("items"));
    }

    @Test
    void showItemsPageWithPriceFilterExcludingItem() {
        String id = UUID.randomUUID().toString();
        CollectibleItem item = new CollectibleItem(id, "Test Item", "Test Description", 100.0);
        when(itemService.getAllItems()).thenReturn(Collections.singletonList(item));
        when(request.queryParams("minPrice")).thenReturn("150");

        String result = itemWebController.showItemsPage(request, response);

        assertFalse(result.contains("Test Item"));
    }
}
