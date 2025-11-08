package org.example;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.example.Model.BroadcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebSocket
public class WebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    @OnWebSocketConnect
    public void onConnect(Session session) {
        logger.info("Cliente conectado: {}", session.getRemoteAddress());
        // Añade la sesión al servicio de broadcast
        BroadcastService.addSession(session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        logger.info("Cliente desconectado: {}", session.getRemoteAddress());
        // Elimina la sesión del servicio de broadcast
        BroadcastService.removeSession(session);
    }


    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        logger.info("Mensaje recibido (no se hará eco): {}", message);

    }
}