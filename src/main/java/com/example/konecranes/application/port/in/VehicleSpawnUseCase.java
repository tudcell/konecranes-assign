package com.example.konecranes.application.port.in;

import java.io.IOException;
import java.util.List;

/**
 * Spawns one or more vehicle JVM processes.
 */
public interface VehicleSpawnUseCase {

    /**
     * Starts the requested number of vehicle processes.
     *
     * @param count number of vehicles to create
     * @return created vehicle ids in spawn order
     * @throws IOException when process launch fails
     */
    List<String> spawn(int count) throws IOException;
}
