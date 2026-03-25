package com.example.konecranes.application;

import com.example.konecranes.model.SimulationSnapshot;
import com.example.konecranes.application.port.in.SimulationSnapshotListener;
import com.example.konecranes.application.port.in.SimulationStreamUseCase;
import com.example.konecranes.application.port.out.SimulationSnapshotPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Application service that manages simulation snapshot subscribers and publishing.
 */
@Service
public class SseSnapshotService implements SimulationStreamUseCase, SimulationSnapshotPublisher {

    private static final Logger logger = LoggerFactory.getLogger(SseSnapshotService.class);

    private final Map<String, SimulationSnapshotListener> listeners = new ConcurrentHashMap<>();

    /**
     * Registers a new snapshot subscriber.
     *
     * @param listener callback to invoke for each snapshot
     * @return generated subscription identifier
     */
    @Override
    public String subscribe(SimulationSnapshotListener listener) {
        String subscriptionId = UUID.randomUUID().toString();
        listeners.put(subscriptionId, listener);
        return subscriptionId;
    }

    /**
     * Removes a snapshot subscriber.
     *
     * @param subscriptionId subscriber id returned by {@link #subscribe(SimulationSnapshotListener)}
     */
    @Override
    public void unsubscribe(String subscriptionId) {
        if (subscriptionId == null) {
            return;
        }
        listeners.remove(subscriptionId);
    }

    /**
     * Publishes one snapshot to all current subscribers.
     *
     * @param snapshot snapshot payload
     */
    @Override
    public void publish(SimulationSnapshot snapshot) {
        for (Map.Entry<String, SimulationSnapshotListener> entry : listeners.entrySet()) {
            try {
                entry.getValue().onSnapshot(snapshot);
            } catch (Exception ex) {
                // Defer removal to avoid ConcurrentModificationException
                listeners.remove(entry.getKey());
                logger.warn("Removed failed stream subscriber {} due to: {}", entry.getKey(), ex.getMessage());
            }
        }
    }
}

