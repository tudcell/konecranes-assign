package com.example.konecranes.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Immutable-style value object describing world bounds.
 */
@Setter
@Getter
public class SimulationWorld {

    private double width;

    private double height;

    public SimulationWorld() {
    }

    public SimulationWorld(double width, double height) {
        this.width = width;
        this.height = height;
    }

}
