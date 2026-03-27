package com.example.konecranes.messaging;

import com.example.konecranes.model.SimulationWorld;
import lombok.Getter;
import lombok.Setter;

/**
 * Registration acknowledgement returned by coordinator to vehicle process.
 */
@Setter
@Getter
public class RegisterVehicleAck {

    private String vehicleId;

    private SimulationWorld world;

}
