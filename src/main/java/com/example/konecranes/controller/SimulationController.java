package com.example.konecranes.controller;

import com.example.konecranes.model.SimulationSnapshot;
import com.example.konecranes.service.port.in.SimulationQueryUseCase;
import com.example.konecranes.service.port.in.SimulationStreamUseCase;
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
        return sseSnapshotService.subscribe();
    }
}
