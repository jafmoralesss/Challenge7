package org.example.Model;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BroadcastService {

    private static final Logger logger = LoggerFactory.getLogger(BroadcastService.class);

    // Usamos CopyOnWriteArrayList para seguridad en concurrencia (thread-safety).
    // Es eficiente cuando hay muchas lecturas (broadcasts) y pocas escrituras (conexiones/desconexiones).
    private static final List<Session> sessions = new CopyOnWriteArrayList<>();

    public static void addSession(Session session) {
        sessions.add(session);
        logger.info("Nueva sesión añadida. Total de sesiones: {}", sessions.size());
    }

    public static void removeSession(Session session) {
        sessions.remove(session);
        logger.info("Sesión eliminada. Total de sesiones: {}", sessions.size());
    }

    /**
     * Envía un mensaje de texto a TODOS los clientes conectados.
     */
    public static void broadcast(String message) {
        logger.info("Iniciando broadcast: {}", message);

        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    // Envía el mensaje a este cliente específico
                    session.getRemote().sendString(message);
                } catch (IOException e) {
                    logger.error("Error al enviar mensaje a la sesión {}: {}", session.getRemoteAddress(), e.getMessage());
                }
            }
        }
    }
}