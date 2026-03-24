package com.example.konecranes.adapter.in.rest;

import com.example.konecranes.model.SimulationSnapshot;
import com.example.konecranes.application.port.in.SimulationQueryUseCase;
import com.example.konecranes.application.port.in.SimulationStreamUseCase;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    private final SimulationQueryUseCase snapshotService;
    private final SimulationStreamUseCase sseSnapshotService;

    public SimulationController(SimulationQueryUseCase snapshotService, SimulationStreamUseCase sseSnapshotService) {
        this.snapshotService = snapshotService;
        this.sseSnapshotService = sseSnapshotService;
    }

    @GetMapping("/snapshot")
    public SimulationSnapshot snapshot() {
        return snapshotService.currentSnapshot();
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(0L);
        String subscriptionId = sseSnapshotService.subscribe(snapshot ->
                emitter.send(SseEmitter.event().name("snapshot").data(snapshot)));

        emitter.onCompletion(() -> sseSnapshotService.unsubscribe(subscriptionId));
        emitter.onTimeout(() -> sseSnapshotService.unsubscribe(subscriptionId));
        emitter.onError(ex -> sseSnapshotService.unsubscribe(subscriptionId));
        return emitter;
    }
}


