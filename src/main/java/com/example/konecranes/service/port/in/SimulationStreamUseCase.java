package com.example.konecranes.service.port.in;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SimulationStreamUseCase {
    SseEmitter subscribe();
}

