package com.example.konecranes.messaging;

import lombok.Getter;
import lombok.Setter;

/**
 * Registration payload sent by a vehicle process when opening a TCP session.
 */
@Setter
@Getter
public class RegisterVehicleRequest {

    private String vehicleId;

    private double initialX;

    private double initialY;

    private double initialDirectionDeg;

    private double initialSpeed;

    private double radius;

}
