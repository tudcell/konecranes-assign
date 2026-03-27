package com.example.konecranes.application;

import com.example.konecranes.application.port.in.SimulationSnapshotListener;
import com.example.konecranes.application.port.in.SimulationStreamUseCase;
import com.example.konecranes.application.port.out.SimulationSnapshotPublisher;
import com.example.konecranes.model.SimulationSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Application service that manages live simulation snapshot subscriptions.
 *
 * Also acts as the publisher for new snapshot events.
 */
@Service
public class SseSnapshotService implements SimulationStreamUseCase, SimulationSnapshotPublisher {

    private static final Logger logger = LoggerFactory.getLogger(SseSnapshotService.class);

    private final Map<String, SimulationSnapshotListener> snapshotListeners = new ConcurrentHashMap<>();

    /**
     * Registers one new snapshot listener.
     *
     * @param listener listener to invoke for each published snapshot
     * @return generated subscription id
     */
    @Override
    public String subscribe(SimulationSnapshotListener listener) {
        String subscriptionId = UUID.randomUUID().toString();
        snapshotListeners.put(subscriptionId, listener);
        return subscriptionId;
    }

    /**
     * Removes one existing snapshot listener.
     *
     * If the subscription id is null or unknown, this method does nothing.
     *
     * @param subscriptionId subscription id returned by subscribe
     */
    @Override
    public void unsubscribe(String subscriptionId) {
        if (subscriptionId == null) {
            return;
        }
        snapshotListeners.remove(subscriptionId);
    }

    /**
     * Publishes one simulation snapshot to all current listeners.
     *
     * If one listener fails, that listener is removed so it does not
     * continue breaking future publish cycles.
     *
     * @param snapshot snapshot to publish
     */
    @Override
    public void publish(SimulationSnapshot snapshot) {
        for (Map.Entry<String, SimulationSnapshotListener> entry : snapshotListeners.entrySet()) {
            try {
                entry.getValue().onSnapshot(snapshot);
            } catch (Exception ex) {
                snapshotListeners.remove(entry.getKey());
                logger.warn("Removed failed stream subscriber {} due to: {}", entry.getKey(), ex.getMessage());
            }
        }
    }
}