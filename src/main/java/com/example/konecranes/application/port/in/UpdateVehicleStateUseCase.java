package com.example.konecranes.application.port.in;

/**
 * Updates the latest known state for a vehicle in the coordinator.
 */
public interface UpdateVehicleStateUseCase {

    /**
     * Applies one vehicle state update received from the vehicle process.
     *
     * @param command state payload containing position, motion, and AI metadata
     */
    void updateState(UpdateVehicleStateCommand command);
}
