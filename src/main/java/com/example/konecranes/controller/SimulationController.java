package com.example.konecranes.controller;

import com.example.konecranes.model.SimulationSnapshot;
import com.example.konecranes.service.SimulationSnapshotService;
import com.example.konecranes.service.SseSnapshotService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    private final SimulationSnapshotService snapshotService;
    private final SseSnapshotService sseSnapshotService;

    public SimulationController(SimulationSnapshotService snapshotService, SseSnapshotService sseSnapshotService) {
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
