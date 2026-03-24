package com.example.konecranes.application.port.out;

/**
 * Compatibility aggregate for existing injections; prefer narrower ports.
 */
public interface VehicleGatewayPort extends VehicleRegistrationGatewayPort,
        VehicleEnvironmentGatewayPort,
        VehicleCommandGatewayPort {
}

