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

@Service
public class SseSnapshotService implements SimulationStreamUseCase, SimulationSnapshotPublisher {

    private static final Logger logger = LoggerFactory.getLogger(SseSnapshotService.class);

    private final Map<String, SimulationSnapshotListener> listeners = new ConcurrentHashMap<>();

    @Override
    public String subscribe(SimulationSnapshotListener listener) {
        String subscriptionId = UUID.randomUUID().toString();
        listeners.put(subscriptionId, listener);
        return subscriptionId;
    }

    @Override
    public void unsubscribe(String subscriptionId) {
        if (subscriptionId == null) {
            return;
        }
        listeners.remove(subscriptionId);
    }

    @Override
    public void publish(SimulationSnapshot snapshot) {
        for (Map.Entry<String, SimulationSnapshotListener> entry : listeners.entrySet()) {
            try {
                entry.getValue().onSnapshot(snapshot);
            } catch (Exception ex) {
                listeners.remove(entry.getKey());
                logger.debug("Removed failed stream subscriber {}", entry.getKey(), ex);
            }
        }
    }
}

