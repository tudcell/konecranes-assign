package com.example.konecranes.application;

import com.example.konecranes.application.port.in.UpdateVehicleStateCommand;
import com.example.konecranes.application.port.out.VehicleStateStore;
import com.example.konecranes.model.AvoidanceAction;
import com.example.konecranes.model.RiskLevel;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class VehicleUpdateServiceTest {

    @Test
    void updateStateCommandMapsToVehicleStateAndPersists() {
        VehicleStateStore stateStore = mock(VehicleStateStore.class);
        VehicleUpdateService service = new VehicleUpdateService(stateStore);

        UpdateVehicleStateCommand command = new UpdateVehicleStateCommand(
                "VH-STATE",
                10.0,
                20.0,
                45.0,
                30.0,
                16.0,
                VehicleStatus.ACTIVE,
                AvoidanceAction.TURN_LEFT,
                RiskLevel.MEDIUM,
                0.63);

        service.updateState(command);

        ArgumentCaptor<VehicleState> stateCaptor = ArgumentCaptor.forClass(VehicleState.class);
        verify(stateStore).upsert(stateCaptor.capture());

        VehicleState persisted = stateCaptor.getValue();
        assertEquals("VH-STATE", persisted.getId());
        assertEquals(10.0, persisted.getX());
        assertEquals(20.0, persisted.getY());
        assertEquals(45.0, persisted.getDirectionDeg());
        assertEquals(30.0, persisted.getSpeed());
        assertEquals(16.0, persisted.getRadius());
        assertEquals(VehicleStatus.ACTIVE, persisted.getStatus());
        assertEquals(AvoidanceAction.TURN_LEFT, persisted.getCurrentAction());
        assertEquals(RiskLevel.MEDIUM, persisted.getRiskLevel());
        assertEquals(0.63, persisted.getCurrentRiskScore());
        assertTrue(persisted.getTimestamp() > 0);
    }
}

