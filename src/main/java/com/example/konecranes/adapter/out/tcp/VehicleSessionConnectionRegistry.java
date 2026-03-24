package com.example.konecranes.adapter.out.tcp;

import java.io.BufferedWriter;

public interface VehicleSessionConnectionRegistry {
    void attach(String vehicleId, BufferedWriter writer);

    void detach(String vehicleId);

    void detachAll();
}


