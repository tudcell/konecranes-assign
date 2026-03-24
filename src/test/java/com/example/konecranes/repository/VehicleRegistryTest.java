package com.example.konecranes.repository;

import com.example.konecranes.adapter.out.persistence.VehicleRegistry;
import com.example.konecranes.model.VehicleState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleRegistryTest {

    @Test
    void findByIdReturnsCopy() {
        VehicleRegistry registry = new VehicleRegistry();
        VehicleState original = new VehicleState();
        original.setId("VH-1");
        original.setX(10.0);

        registry.upsert(original);

        VehicleState fromStore = registry.findById("VH-1").orElseThrow(IllegalStateException::new);
        fromStore.setX(99.0);

        VehicleState secondRead = registry.findById("VH-1").orElseThrow(IllegalStateException::new);
        assertEquals(10.0, secondRead.getX());
        assertNotSame(fromStore, secondRead);
    }

    @Test
    void findAllExceptFiltersVehicle() {
        VehicleRegistry registry = new VehicleRegistry();

        VehicleState one = new VehicleState();
        one.setId("VH-1");
        registry.upsert(one);

        VehicleState two = new VehicleState();
        two.setId("VH-2");
        registry.upsert(two);

        List<VehicleState> result = registry.findAllExcept("VH-1");
        assertEquals(1, result.size());
        assertEquals("VH-2", result.get(0).getId());
    }

    @Test
    void removeDeletesVehicle() {
        VehicleRegistry registry = new VehicleRegistry();
        VehicleState state = new VehicleState();
        state.setId("VH-1");
        registry.upsert(state);

        registry.remove("VH-1");

        assertTrue(registry.findById("VH-1").isEmpty());
    }
}

