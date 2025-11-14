package org.example;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.example.model.BroadcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles WebSocket connections for real-time notifications.
 * <p>
 * This class is annotated with {@link WebSocket} and is registered with SparkJava
 * to manage the lifecycle of client connections on the `/notifications` endpoint.
 * Its primary responsibility is to add and remove client sessions from the
 * {@link BroadcastService}, which is then used by other parts of the application
 * (like the {@code OfferService}) to push updates to all connected clients.
 */
@WebSocket
public class WebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    /**
     * Called when a new WebSocket connection is established.
     * This method is triggered by the {@link OnWebSocketConnect} annotation.
     * It logs the new connection and adds the client's {@link Session} to the
     * list of active sessions in the {@link BroadcastService}.
     *
     * @param session The newly connected client's session.
     */
    @OnWebSocketConnect
    public void onConnect(Session session) {
        logger.info("Cliente conectado: {}", session.getRemoteAddress());
        // Añade la sesión al servicio de broadcast
        BroadcastService.addSession(session);
    }

    /**
     * Called when a WebSocket connection is closed.
     * This method is triggered by the {@link OnWebSocketClose} annotation.
     * It logs the disconnection and removes the client's {@link Session} from
     * the list of active sessions in the {@link BroadcastService}.
     *
     * @param session    The client session that is disconnecting.
     * @param statusCode The HTTP status code indicating the reason for closure.
     * @param reason     A text string explaining the reason for closure.
     */
    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        logger.info("Cliente desconectado: {}", session.getRemoteAddress());
        // Elimina la sesión del servicio de broadcast
        BroadcastService.removeSession(session);
    }

    /**
     * Called when a message is received from a WebSocket client.
     * This method is triggered by the {@link OnWebSocketMessage} annotation.
     * <p>
     * In this implementation, the server simply logs that a message was received
     * but does **not** process it or send a response. The communication is
     * one-way (server-to-client broadcasts).
     *
     * @param session The client session that sent the message.
     * @param message The text message received from the client.
     */
    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        logger.info("Mensaje recibido (no se hará eco): {}", message);

    }
}