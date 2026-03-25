package com.example.konecranes.vehicle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VehicleBehaviorEngineTest {
    @Test
    void canInstantiateAndUpdateEnvironment() {
        VehicleProcessConfig config = VehicleProcessConfig.fromArgs(new String[]{"--vehicleId=VH-UNITTEST"});
        VehicleBehaviorEngine engine = new VehicleBehaviorEngine(config);
        assertNotNull(engine);
        // Test onEnvironmentUpdate with empty update
        com.example.konecranes.messaging.EnvironmentUpdate update = new com.example.konecranes.messaging.EnvironmentUpdate();
        update.setNearbyVehicles(java.util.Collections.emptyList());
        engine.onEnvironmentUpdate(update);
    }
}
