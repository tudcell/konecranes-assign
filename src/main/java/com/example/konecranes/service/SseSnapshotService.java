package com.example.konecranes.service;

import com.example.konecranes.model.SimulationSnapshot;
import com.example.konecranes.service.port.in.SimulationStreamUseCase;
import com.example.konecranes.service.port.out.SimulationSnapshotPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseSnapshotService implements SimulationStreamUseCase, SimulationSnapshotPublisher {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @Override
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(ex -> emitters.remove(emitter));
        return emitter;
    }

    @Override
    public void publish(SimulationSnapshot snapshot) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("snapshot").data(snapshot));
            } catch (IOException ex) {
                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }
}
