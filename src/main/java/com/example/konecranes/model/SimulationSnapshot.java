package com.example.konecranes.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Read-model payload representing one rendered simulation frame.
 */
@Setter
@Getter
public class SimulationSnapshot {

    private long generatedAt;

    private SimulationWorld world;

    private List<VehicleState> vehicles;

    private int collisionWarnings;

}
