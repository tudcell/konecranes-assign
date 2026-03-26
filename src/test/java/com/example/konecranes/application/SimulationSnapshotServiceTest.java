package com.example.konecranes.application;

import com.example.konecranes.application.port.out.VehicleStateRepository;
import com.example.konecranes.config.SimulationProperties;
import com.example.konecranes.model.RiskLevel;
import com.example.konecranes.model.SimulationSnapshot;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimulationSnapshotServiceTest {

    @Test
    void currentSnapshotBuildsWorldAndHighRiskCount() {
        VehicleStateRepository stateStore = mock(VehicleStateRepository.class);

        VehicleState highRisk = new VehicleState();
        highRisk.setId("VH-HIGH");
        highRisk.setRiskLevel(RiskLevel.HIGH);
        highRisk.setStatus(VehicleStatus.ACTIVE);

        VehicleState lowRisk = new VehicleState();
        lowRisk.setId("VH-LOW");
        lowRisk.setRiskLevel(RiskLevel.LOW);
        lowRisk.setStatus(VehicleStatus.ACTIVE);

        VehicleState disconnected = new VehicleState();
        disconnected.setId("VH-DISCONNECTED");
        disconnected.setRiskLevel(RiskLevel.HIGH);
        disconnected.setStatus(VehicleStatus.DISCONNECTED);

        when(stateStore.findAll()).thenReturn(Arrays.asList(highRisk, lowRisk, disconnected));

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

