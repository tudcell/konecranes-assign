package com.example.konecranes.vehicle;

/**
 * Standalone entry point used by spawned vehicle JVM processes.
 */
public class VehicleProcessMain {

    /**
     * Builds process configuration from arguments and starts runtime loop.
     *
     * @param args command line arguments in {@code --key=value} format
     */
    public static void main(String[] args) {
        VehicleProcessConfig config = VehicleProcessConfig.fromArgs(args);
        VehicleProcessRuntime runtime = new VehicleProcessRuntime(config);
        runtime.start();
    }
}
