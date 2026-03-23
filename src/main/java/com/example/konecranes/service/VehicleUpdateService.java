package com.example.konecranes.service;

import com.example.konecranes.model.VehicleState;
import com.example.konecranes.service.port.out.VehicleStateStore;
import org.springframework.stereotype.Service;

@Service
public class VehicleUpdateService {

    private final VehicleStateStore vehicleStateStore;

    public VehicleUpdateService(VehicleStateStore vehicleStateStore) {
        this.vehicleStateStore = vehicleStateStore;
    }

    public void updateState(VehicleState state) {
        state.setTimestamp(System.currentTimeMillis());
        vehicleStateStore.upsert(state);
    }
}
