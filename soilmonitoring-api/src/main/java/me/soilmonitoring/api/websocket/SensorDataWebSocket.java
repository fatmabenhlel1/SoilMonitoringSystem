package me.soilmonitoring.api.websocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@ServerEndpoint("/ws/sensor-data")
@ApplicationScoped
public class SensorDataWebSocket {

    private static final Logger logger = Logger.getLogger(SensorDataWebSocket.class.getName());

    // Thread-safe set to track all connected clients
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        logger.info("✅ WebSocket client connected: " + session.getId() +
                " | Total clients: " + sessions.size());

        // Send welcome message
        try {
            session.getBasicRemote().sendText(
                    "{\"type\":\"CONNECTION\",\"message\":\"Connected to soil monitoring system\"}"
            );
        } catch (IOException e) {
            logger.warning("Failed to send welcome message: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        sessions.remove(session);
        logger.info("❌ WebSocket client disconnected: " + session.getId() +
                " | Reason: " + reason.getReasonPhrase() +
                " | Remaining clients: " + sessions.size());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("📨 Received message from client " + session.getId() + ": " + message);
        // Handle client messages if needed (e.g., subscribe to specific field)
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.severe("❌ WebSocket error for session " + session.getId() +
                ": " + throwable.getMessage());
        sessions.remove(session);
    }

    /**
     * Broadcast message to all connected WebSocket clients
     */
    public static void broadcast(String message) {
        synchronized (sessions) {
            int successCount = 0;
            int failCount = 0;

            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(message);
                        successCount++;
                    } catch (IOException e) {
                        logger.warning("Failed to send to session " + session.getId() +
                                ": " + e.getMessage());
                        failCount++;
                    }
                } else {
                    failCount++;
                }
            }

            if (successCount > 0) {
                logger.info("📡 Broadcast to " + successCount + " clients" +
                        (failCount > 0 ? " (" + failCount + " failed)" : ""));
            }
        }
    }

    /**
     * Get number of connected clients
     */
    public static int getConnectedClientsCount() {
        return sessions.size();
    }
}