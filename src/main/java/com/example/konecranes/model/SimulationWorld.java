package com.example.konecranes.model;

/**
 * Immutable-style value object describing world bounds.
 */
public class SimulationWorld {
    private double width;
    private double height;

    public SimulationWorld() {
    }

    public SimulationWorld(double width, double height) {
        this.width = width;
        this.height = height;
    }

    /** @return world width */
    public double getWidth() {
        return width;
    }

    /** @param width world width */
    public void setWidth(double width) {
        this.width = width;
    }

    /** @return world height */
    public double getHeight() {
        return height;
    }

    /** @param height world height */
    public void setHeight(double height) {
        this.height = height;
    }
}
