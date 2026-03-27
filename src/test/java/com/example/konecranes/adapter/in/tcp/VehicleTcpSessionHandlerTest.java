package com.example.konecranes.adapter.in.tcp;

import com.example.konecranes.application.port.in.DisconnectVehicleSessionUseCase;
import com.example.konecranes.application.port.in.RegisterVehicleSessionUseCase;
import com.example.konecranes.application.port.in.UpdateVehicleStateUseCase;
import com.example.konecranes.application.port.out.VehicleSessionRegistryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.Socket;
import static org.mockito.Mockito.*;

class VehicleTcpSessionHandlerTest {
    @Test
    void handle_closesSocketAndDetachesVehicle() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        RegisterVehicleSessionUseCase reg = mock(RegisterVehicleSessionUseCase.class);
        DisconnectVehicleSessionUseCase dis = mock(DisconnectVehicleSessionUseCase.class);
        UpdateVehicleStateUseCase upd = mock(UpdateVehicleStateUseCase.class);
        VehicleSessionRegistryPort regPort = mock(VehicleSessionRegistryPort.class);
        VehicleTcpSessionHandler handler = new VehicleTcpSessionHandler(objectMapper, reg, dis, upd, regPort);
        Socket socket = mock(Socket.class);
        // Simulate IOException on getInputStream to exit early
        when(socket.getInputStream()).thenThrow(new IOException("fail"));
        handler.handle(socket);
        verify(socket, atLeastOnce()).close();
        // No vehicleId, so detach should not be called
        verify(regPort, never()).detach(anyString());
    }
}
