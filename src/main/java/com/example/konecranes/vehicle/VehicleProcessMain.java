package com.example.konecranes.vehicle;

public class VehicleProcessMain {

    public static void main(String[] args) {
        VehicleProcessConfig config = VehicleProcessConfig.fromArgs(args);
        VehicleProcessRuntime runtime = new VehicleProcessRuntime(config);
        runtime.start();
    }
}
