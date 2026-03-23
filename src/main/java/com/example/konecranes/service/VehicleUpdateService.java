package com.example.konecranes.service;

import com.example.konecranes.model.VehicleState;
import org.springframework.stereotype.Service;

@Service
public class VehicleUpdateService {

    private final VehicleRegistry vehicleRegistry;

    public VehicleUpdateService(VehicleRegistry vehicleRegistry) {
        this.vehicleRegistry = vehicleRegistry;
    }

    public void updateState(VehicleState state) {
        state.setTimestamp(System.currentTimeMillis());
        vehicleRegistry.upsert(state);
    }
}
