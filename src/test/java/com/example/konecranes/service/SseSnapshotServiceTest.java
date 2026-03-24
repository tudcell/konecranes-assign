package com.example.konecranes.service;

import com.example.konecranes.application.SseSnapshotService;
import com.example.konecranes.model.SimulationSnapshot;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SseSnapshotServiceTest {

    @Test
    void publishNotifiesActiveSubscribersAndStopsAfterUnsubscribe() {
        SseSnapshotService service = new SseSnapshotService();
        AtomicInteger counter = new AtomicInteger();

        String subscriptionId = service.subscribe(snapshot -> counter.incrementAndGet());

        service.publish(new SimulationSnapshot());
        assertEquals(1, counter.get());

        service.unsubscribe(subscriptionId);
        service.publish(new SimulationSnapshot());
        assertEquals(1, counter.get());
    }

    @Test
    void publishRemovesFailingSubscriber() {
        SseSnapshotService service = new SseSnapshotService();
        AtomicInteger healthyCounter = new AtomicInteger();

        service.subscribe(snapshot -> {
            throw new IllegalStateException("boom");
        });
        service.subscribe(snapshot -> healthyCounter.incrementAndGet());

        service.publish(new SimulationSnapshot());
        service.publish(new SimulationSnapshot());

        assertEquals(2, healthyCounter.get());
    }
}

