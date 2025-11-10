package org.example.Controller;

import org.example.Model.ApiException;
import org.example.Model.CollectibleItem;
import org.example.Model.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spark.Request;
import spark.Response;
import spark.Session;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

/**
 * Test Unit for ItemWebController.
 * Will use Mockito to mock the dependencies, so we can test the controller.
 */
@ExtendWith(MockitoExtension.class)
class ItemWebControllerTest {


    @Mock
    private ItemService mockItemService;
    @Mock
    private Request mockRequest;
    @Mock
    private Response mockResponse;
    @Mock
    private Session mockSession;


    @InjectMocks
    private ItemWebController itemWebController;

    @BeforeEach
    void setUp() {

        when(mockRequest.session(true)).thenReturn(mockSession);

        when(mockRequest.session()).thenReturn(mockSession);
    }

    /**
     * TASK 2.3 (GOOD RESPONSE)
     * A valid form will be sent and the article will be successfully created.
     */
    @Test
    void testHandleItemForm_Success() {

        when(mockRequest.queryParams("itemName")).thenReturn("Test Item");
        when(mockRequest.queryParams("itemDescription")).thenReturn("Test Desc");
        when(mockRequest.queryParams("itemImageUrl")).thenReturn("");
        when(mockRequest.queryParams("itemPrice")).thenReturn("123.45");

        String result = itemWebController.handleItemForm(mockRequest, mockResponse);

        verify(mockItemService).createItem(anyString(), any(CollectibleItem.class));

        verify(mockSession).attribute("successMessage", "New item 'Test Item' added successfully!");

        verify(mockResponse).redirect("/items-web");

        assertNull(result);
    }

    /**
     * TASK 2.4 (NEGATIVE RESPONSE - USER ERROR)
     * User will introduce a nonvalid price.
     */
    @Test
    void testHandleItemForm_Fail_InvalidPriceFormat() {

        when(mockRequest.queryParams("itemName")).thenReturn("Bad Item");
        when(mockRequest.queryParams("itemDescription")).thenReturn("Test Desc");
        when(mockRequest.queryParams("itemImageUrl")).thenReturn("");
        when(mockRequest.queryParams("itemPrice")).thenReturn("not-a-number"); // <-- El error

        String result = itemWebController.handleItemForm(mockRequest, mockResponse);

        verify(mockItemService, never()).createItem(any(), any());


        verify(mockSession).attribute("errorMessage", "Invalid price format. Please enter a valid number.");

        verify(mockResponse).redirect("/items-web");
        assertNull(result);
    }

    /**
     * TASK 2.5 (NEGATIVE RESPONSE - System exception)
     * Any service throws an exception.
     */
    @Test
    void testHandleItemForm_Fail_ApiException() {

        when(mockRequest.queryParams("itemName")).thenReturn("Test Item");
        when(mockRequest.queryParams("itemDescription")).thenReturn("Test Desc");
        when(mockRequest.queryParams("itemImageUrl")).thenReturn("");
        when(mockRequest.queryParams("itemPrice")).thenReturn("123.45");

        ApiException dbExploded = new ApiException(500, "Database exploded");

        doThrow(dbExploded).when(mockItemService).createItem(anyString(), any(CollectibleItem.class));

        String result = itemWebController.handleItemForm(mockRequest, mockResponse);

        verify(mockItemService).createItem(anyString(), any(CollectibleItem.class));

        verify(mockSession).attribute("errorMessage", "Database exploded");

        verify(mockResponse).redirect("/items-web");
        assertNull(result);
    }
}