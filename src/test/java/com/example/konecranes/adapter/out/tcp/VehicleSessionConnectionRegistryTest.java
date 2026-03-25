package com.example.konecranes.adapter.out.tcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.io.BufferedWriter;
import java.io.IOException;
import static org.mockito.Mockito.*;

class VehicleSessionConnectionRegistryTest {
    @Test
    void attachAndDetachConnection() throws IOException {
        VehicleConnectionManager registry = new VehicleConnectionManager(new ObjectMapper());
        BufferedWriter writer = mock(BufferedWriter.class);
        String vehicleId = "VH-UNITTEST";
        registry.attach(vehicleId, writer);
        // Detach should close the writer
        registry.detach(vehicleId);
        verify(writer, times(1)).close();
        // Detaching again should not throw
        registry.detach(vehicleId);
    }

    @Test
    void detachAllClosesAllWriters() throws IOException {
        VehicleConnectionManager registry = new VehicleConnectionManager(new ObjectMapper());
        BufferedWriter writer1 = mock(BufferedWriter.class);
        BufferedWriter writer2 = mock(BufferedWriter.class);
        registry.attach("VH-1", writer1);
        registry.attach("VH-2", writer2);
        registry.detachAll();
        verify(writer1, times(1)).close();
        verify(writer2, times(1)).close();
    }
}
