package com.example.konecranes.service.port.in;

import com.example.konecranes.model.SimulationSnapshot;

public interface SimulationQueryUseCase {
    SimulationSnapshot currentSnapshot();
}

