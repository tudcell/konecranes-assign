package com.example.konecranes.service;

import com.example.konecranes.application.SimulationSnapshotService;
import com.example.konecranes.application.port.out.VehicleStateStore;
import com.example.konecranes.config.SimulationProperties;
import com.example.konecranes.model.RiskLevel;
import com.example.konecranes.model.SimulationSnapshot;
import com.example.konecranes.model.VehicleState;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimulationSnapshotServiceTest {

    @Test
    void currentSnapshotBuildsWorldAndHighRiskCount() {
        VehicleStateStore stateStore = mock(VehicleStateStore.class);

        VehicleState highRisk = new VehicleState();
        highRisk.setId("VH-HIGH");
        highRisk.setRiskLevel(RiskLevel.HIGH);

        VehicleState lowRisk = new VehicleState();
        lowRisk.setId("VH-LOW");
        lowRisk.setRiskLevel(RiskLevel.LOW);

        when(stateStore.findAll()).thenReturn(Arrays.asList(highRisk, lowRisk));

        SimulationProperties properties = new SimulationProperties();
        properties.getWorld().setWidth(1000.0);
        properties.getWorld().setHeight(700.0);

        SimulationSnapshotService service = new SimulationSnapshotService(stateStore, properties);
        SimulationSnapshot snapshot = service.currentSnapshot();

        assertEquals(2, snapshot.getVehicles().size());
        assertEquals(1, snapshot.getCollisionWarnings());
        assertEquals(1000.0, snapshot.getWorld().getWidth());
        assertEquals(700.0, snapshot.getWorld().getHeight());
        assertTrue(snapshot.getGeneratedAt() > 0);
    }
}

