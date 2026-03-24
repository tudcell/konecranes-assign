package com.example.konecranes.application.port.out;

import com.example.konecranes.model.VehicleState;

import java.util.List;
import java.util.Optional;

public interface VehicleStateStore {
    void upsert(VehicleState state);

    Optional<VehicleState> findById(String vehicleId);

    List<VehicleState> findAll();

    List<VehicleState> findAllExcept(String vehicleId);

    void remove(String vehicleId);
}


