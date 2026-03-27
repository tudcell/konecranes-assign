package com.example.konecranes.application.port.in;

/**
 * Use case for updating the latest known state of a vehicle.
 *
 * Called when the coordinator receives a new state update
 * from a connected vehicle process.
 */
public interface UpdateVehicleStateUseCase {

    /**
     * Applies one vehicle state update.
     *
     * @param command latest vehicle state payload
     */
    void updateState(UpdateVehicleStateCommand command);
}