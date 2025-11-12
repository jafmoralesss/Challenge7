package org.example.Controller;

import com.google.gson.Gson;
import org.example.Model.Offer;
import org.example.Model.OfferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import spark.Request;
import spark.Response;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OfferControllerTest {

    @Mock
    private OfferService offerService;

    @Mock
    private Request req;

    @Mock
    private Response res;

    @InjectMocks
    private OfferController offerController;

    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllOffers() {
        when(offerService.getAllOffers()).thenReturn(Collections.emptyList());
        String result = offerController.getAllOffers(req, res);
        verify(res).type("application/json");
        assertEquals("[]", result);
    }

    @Test
    void getOfferById() {
        UUID id = UUID.randomUUID();
        Offer offer = new Offer();
        offer.setId(id);
        when(req.params(":id")).thenReturn(id.toString());
        when(offerService.getOfferById(id)).thenReturn(offer);
        String result = offerController.getOfferById(req, res);
        verify(res).type("application/json");
        assertEquals(gson.toJson(offer), result);
    }

    @Test
    void createOffer() {
        UUID id = UUID.randomUUID();
        Offer offer = new Offer();
        offer.setId(id);
        String jsonOffer = gson.toJson(offer);

        when(req.params(":id")).thenReturn(id.toString());
        when(req.body()).thenReturn(jsonOffer);
        when(offerService.createOffer(any(Offer.class))).thenReturn(offer);

        String result = offerController.createOffer(req, res);

        verify(res).type("application/json");
        verify(res).status(201);
        assertEquals(jsonOffer, result);
    }

    @Test
    void updateOffer() {
        UUID id = UUID.randomUUID();
        Offer offer = new Offer();
        offer.setId(id);
        String jsonOffer = gson.toJson(offer);

        when(req.params(":id")).thenReturn(id.toString());
        when(req.body()).thenReturn(jsonOffer);
        when(offerService.updateOffer(eq(id), any(Offer.class))).thenReturn(offer);

        String result = offerController.updateOffer(req, res);

        verify(res).type("application/json");
        assertEquals(jsonOffer, result);
    }

    @Test
    void deleteOffer() {
        UUID id = UUID.randomUUID();
        when(req.params(":id")).thenReturn(id.toString());

        String result = offerController.deleteOffer(req, res);

        verify(offerService).deleteOffer(id);
        verify(res).type("application/json");
        assertEquals("{\"message\":\"Offer deleted successfully\"}", result);
    }
}