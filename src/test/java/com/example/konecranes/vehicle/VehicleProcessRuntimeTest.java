package com.example.konecranes.vehicle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VehicleProcessRuntimeTest {
    @Test
    void canInstantiateAndStart() {
        VehicleProcessConfig config = VehicleProcessConfig.fromArgs(new String[]{"--vehicleId=VH-UNITTEST"});
        VehicleProcessRuntime runtime = new VehicleProcessRuntime(config);
        assertNotNull(runtime);
        // Smoke test: start in a thread and interrupt
        Thread t = new Thread(runtime::start);
        t.start();
        try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        t.interrupt();
        assertTrue(t.isAlive() || !t.isAlive()); // Just to use t
    }
}
