package com.example.konecranes.adapter.out.persistence;

import com.example.konecranes.model.VehicleState;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryVehicleStateRepositoryTest {
    @Test
    void upsertAndFindById_returnsCopy() {
        InMemoryVehicleStateRepository repo = new InMemoryVehicleStateRepository();
        VehicleState state = new VehicleState();
        state.setId("VH-1");
        state.setX(1);
        state.setY(2);
        repo.upsert(state);
        Optional<VehicleState> foundOpt = repo.findById("VH-1");
        assertTrue(foundOpt.isPresent());
        VehicleState found = foundOpt.get();
        assertNotSame(state, found);
        assertEquals(state.getId(), found.getId());
    }

    @Test
    void findAll_returnsCopies() {
        InMemoryVehicleStateRepository repo = new InMemoryVehicleStateRepository();
        VehicleState s1 = new VehicleState();
        s1.setId("VH-1");
        VehicleState s2 = new VehicleState();
        s2.setId("VH-2");
        repo.upsert(s1);
        repo.upsert(s2);
        for (VehicleState s : repo.findAll()) {
            assertNotNull(s.getId());
        }
    }
}
