package com.example.konecranes.messaging;

import lombok.Getter;
import lombok.Setter;

/**
 * Command payload used to apply manual control on a vehicle process.
 */
@Setter
@Getter
public class ControlCommand {

    private String vehicleId;

    private Double overrideDirectionDeg;

    private Double overrideSpeed;

    private boolean manualOverride;

}
