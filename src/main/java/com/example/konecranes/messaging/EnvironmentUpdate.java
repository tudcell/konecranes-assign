package com.example.konecranes.messaging;

import com.example.konecranes.model.VehicleState;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Environment snapshot sent by coordinator to each vehicle process.
 */
@Setter
@Getter
public class EnvironmentUpdate {

    private List<VehicleState> nearbyVehicles;

    private long timestamp;

}
