package com.example.konecranes.application.port.in;

import com.example.konecranes.model.SimulationSnapshot;

public interface SimulationQueryUseCase {
    SimulationSnapshot currentSnapshot();
}


