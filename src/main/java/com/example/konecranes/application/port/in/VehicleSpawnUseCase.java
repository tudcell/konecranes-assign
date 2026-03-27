package com.example.konecranes.application.port.in;

import java.io.IOException;
import java.util.List;

/**
 * Use case for spawning one or more vehicle processes.
 *
 * Starts new vehicle JVM instances and returns
 * their assigned vehicle identifiers.
 */
public interface VehicleSpawnUseCase {

    /**
     * Spawns the requested number of vehicle processes.
     *
     * @param count number of vehicles to create
     * @return created vehicle ids in spawn order
     * @throws IOException when process launch fails
     */
    List<String> spawn(int count) throws IOException;
}