package org.example.controller;

import org.example.model.ApiException;
import org.example.model.Offer;
import org.example.model.OfferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Request;
import spark.Response;
import spark.Session;

import static org.mockito.Mockito.*;

public class OfferWebControllerTest {

    private OfferService offerService;
    private OfferWebController offerWebController;
    private Request request;
    private Response response;
    private Session session;

    @BeforeEach
    void setUp() {
        offerService = mock(OfferService.class);
        offerWebController = new OfferWebController(offerService);
        request = mock(Request.class);
        response = mock(Response.class);
        session = mock(Session.class);

        when(request.session(true)).thenReturn(session);
        when(request.session()).thenReturn(session);
    }

    @Test
    void handleOfferForm_success() throws ApiException {
        when(request.queryParams("name")).thenReturn("John Doe");
        when(request.queryParams("email")).thenReturn("john.doe@example.com");
        when(request.queryParams("itemPrice")).thenReturn("100.0");
        when(request.queryParams("item-id")).thenReturn("item-123");

        offerWebController.handleOfferForm(request, response);

        verify(offerService).createOffer(any(Offer.class));
        verify(session).attribute("successMessage", "Offer submitted successfully.");
        verify(response).redirect("/items-web");
    }

    @Test
    void handleOfferForm_apiException() throws ApiException {
        when(request.queryParams("name")).thenReturn("John Doe");
        when(request.queryParams("email")).thenReturn("john.doe@example.com");
        when(request.queryParams("itemPrice")).thenReturn("100.0");
        when(request.queryParams("item-id")).thenReturn("item-123");

        doThrow(new ApiException(500, "Test API Exception")).when(offerService).createOffer(any(Offer.class));

        offerWebController.handleOfferForm(request, response);

        verify(session).attribute("errorMessage", "Test API Exception");
        verify(response).redirect("/items-web");
    }

    @Test
    void handleOfferForm_numberFormatException() {
        when(request.queryParams("name")).thenReturn("John Doe");
        when(request.queryParams("email")).thenReturn("john.doe@example.com");
        when(request.queryParams("itemPrice")).thenReturn("invalid-price");
        when(request.queryParams("item-id")).thenReturn("item-123");

        offerWebController.handleOfferForm(request, response);

        verify(session).attribute("errorMessage", "Invalid price format");
        verify(response).redirect("/items-web");
    }
}
