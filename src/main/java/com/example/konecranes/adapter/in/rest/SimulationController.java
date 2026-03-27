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
 * REST controller for reading simulation state.
 *
 * Exposes:
 * - one endpoint for the latest snapshot
 * - one SSE endpoint for live simulation updates
 */
@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    private final SimulationQueryUseCase simulationQueryUseCase;
    private final SimulationStreamUseCase simulationStreamUseCase;
    private final SseProperties sseProperties;

    public SimulationController(SimulationQueryUseCase simulationQueryUseCase,
                                SimulationStreamUseCase simulationStreamUseCase,
                                SseProperties sseProperties) {
        this.simulationQueryUseCase = simulationQueryUseCase;
        this.simulationStreamUseCase = simulationStreamUseCase;
        this.sseProperties = sseProperties;
    }

    /**
     * Returns the current simulation snapshot.
     *
     * Used by clients that want the latest state immediately
     * without opening a streaming connection.
     *
     * @return current simulation snapshot
     */
    @GetMapping("/snapshot")
    public SimulationSnapshot snapshot() {
        return simulationQueryUseCase.currentSnapshot();
    }

    /**
     * Opens a Server-Sent Events stream for live simulation updates.
     *
     * A new emitter is created for each client connection.
     * The client is subscribed to snapshot events and automatically
     * unsubscribed when the connection completes, times out, or fails.
     *
     * @return SSE emitter streaming snapshot events
     */
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(sseProperties.getEmitterTimeoutMillis());

        String subscriptionId = simulationStreamUseCase.subscribe(snapshot -> {
            try {
                emitter.send(SseEmitter.event().name("snapshot").data(snapshot));
            } catch (IOException ex) {
                // The client connection is no longer writable.
                // Unsubscription is handled through the emitter callbacks below.
                throw new RuntimeException(ex);
            }
        });

        // Clean up subscription when the client disconnects or the stream ends.
        emitter.onCompletion(() -> simulationStreamUseCase.unsubscribe(subscriptionId));
        emitter.onTimeout(() -> simulationStreamUseCase.unsubscribe(subscriptionId));
        emitter.onError(ex -> simulationStreamUseCase.unsubscribe(subscriptionId));

        return emitter;
    }
}