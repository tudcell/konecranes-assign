package com.example.konecranes.application.port.out;

import java.io.IOException;
import java.util.List;

/**
 * Outbound port responsible for starting a vehicle process.
 */
public interface VehicleProcessLauncherPort {

    /**
     * Launches a new process using the provided command line.
     *
     * @param command executable plus arguments
     * @return process handle used for lifecycle management
     * @throws IOException when process creation fails
     */
    VehicleProcessHandle launch(List<String> command) throws IOException;
}
