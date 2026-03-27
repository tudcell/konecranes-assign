package com.example.konecranes.vehicle;

/**
 * Entry point for a spawned vehicle JVM process.
 *
 * Parses the process configuration from command-line arguments
 * and starts the vehicle runtime loop.
 */
public class VehicleProcessMain {

    /**
     * Starts one vehicle process.
     *
     * @param args command-line arguments in --key=value format
     */
    public static void main(String[] args) {
        VehicleProcessConfig config = VehicleProcessConfig.fromArgs(args);
        VehicleProcessRuntime runtime = new VehicleProcessRuntime(config);
        runtime.start();
    }
}