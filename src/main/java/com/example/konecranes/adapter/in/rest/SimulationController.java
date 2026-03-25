package com.example.konecranes.adapter.in.rest;

import com.example.konecranes.application.port.in.SimulationQueryUseCase;
import com.example.konecranes.application.port.in.SimulationStreamUseCase;
import com.example.konecranes.config.SseProperties;
import com.example.konecranes.model.SimulationSnapshot;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * REST adapter exposing simulation read endpoints.
 */
@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    private final SimulationQueryUseCase snapshotService;
    private final SimulationStreamUseCase sseSnapshotService;
    private final SseProperties sseProperties;

    public SimulationController(SimulationQueryUseCase snapshotService,
                                SimulationStreamUseCase sseSnapshotService,
                                SseProperties sseProperties) {
        this.snapshotService = snapshotService;
        this.sseSnapshotService = sseSnapshotService;
        this.sseProperties = sseProperties;
    }

    /**
     * Returns the current simulation snapshot.
     *
     * @return current snapshot model
     */
    @GetMapping("/snapshot")
    public SimulationSnapshot snapshot() {
        return snapshotService.currentSnapshot();
    }

    /**
     * Opens a Server-Sent Events stream for live snapshots.
     *
     * @return configured SSE emitter subscribed to snapshot events
     */
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(sseProperties.getEmitterTimeoutMillis());
        String subscriptionId = sseSnapshotService.subscribe(snapshot -> {
            try {
                emitter.send(SseEmitter.event().name("snapshot").data(snapshot));
            } catch (IOException ex) {
                // Connection lost, will be handled by emitter.onError callback
                throw new RuntimeException(ex);
            }
        });

        emitter.onCompletion(() -> sseSnapshotService.unsubscribe(subscriptionId));
        emitter.onTimeout(() -> sseSnapshotService.unsubscribe(subscriptionId));
        emitter.onError(ex -> sseSnapshotService.unsubscribe(subscriptionId));
        return emitter;
    }
}
