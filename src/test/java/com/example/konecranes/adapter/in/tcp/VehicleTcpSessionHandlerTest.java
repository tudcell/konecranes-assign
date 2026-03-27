package com.example.konecranes.adapter.in.tcp;

import com.example.konecranes.application.port.in.DisconnectVehicleSessionCommand;
import com.example.konecranes.application.port.in.DisconnectVehicleSessionUseCase;
import com.example.konecranes.application.port.in.RegisterVehicleSessionCommand;
import com.example.konecranes.application.port.in.RegisterVehicleSessionUseCase;
import com.example.konecranes.application.port.in.UpdateVehicleStateCommand;
import com.example.konecranes.application.port.in.UpdateVehicleStateUseCase;
import com.example.konecranes.application.port.out.VehicleSessionChannel;
import com.example.konecranes.application.port.out.VehicleSessionRegistryPort;
import com.example.konecranes.messaging.MessageType;
import com.example.konecranes.messaging.RegisterVehicleRequest;
import com.example.konecranes.messaging.WireMessage;
import com.example.konecranes.model.AvoidanceAction;
import com.example.konecranes.model.RiskLevel;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VehicleTcpSessionHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void handleRegisterMessageAttachesSessionInvokesRegisterAndCleansUp() throws Exception {
        RegisterVehicleSessionUseCase registerVehicleSessionUseCase = mock(RegisterVehicleSessionUseCase.class);
        DisconnectVehicleSessionUseCase disconnectVehicleSessionUseCase = mock(DisconnectVehicleSessionUseCase.class);
        UpdateVehicleStateUseCase updateVehicleStateUseCase = mock(UpdateVehicleStateUseCase.class);
        VehicleSessionRegistryPort vehicleSessionRegistryPort = mock(VehicleSessionRegistryPort.class);

        VehicleTcpSessionHandler handler = new VehicleTcpSessionHandler(
                objectMapper,
                registerVehicleSessionUseCase,
                disconnectVehicleSessionUseCase,
                updateVehicleStateUseCase,
                vehicleSessionRegistryPort
        );

        RegisterVehicleRequest request = new RegisterVehicleRequest();
        request.setVehicleId("VH-REG");
        request.setInitialX(10.0);
        request.setInitialY(20.0);
        request.setInitialDirectionDeg(90.0);
        request.setInitialSpeed(50.0);
        request.setRadius(16.0);

        WireMessage registerMessage = new WireMessage(MessageType.REGISTER, request);

        Socket socket = mockSocketWithInputLines(
                objectMapper.writeValueAsString(registerMessage)
        );

        handler.handle(socket);

        ArgumentCaptor<RegisterVehicleSessionCommand> registerCaptor =
                ArgumentCaptor.forClass(RegisterVehicleSessionCommand.class);

        verify(registerVehicleSessionUseCase).register(registerCaptor.capture());
        RegisterVehicleSessionCommand command = registerCaptor.getValue();

        assertEquals("VH-REG", command.getVehicleId());
        assertEquals(10.0, command.getInitialX());
        assertEquals(20.0, command.getInitialY());
        assertEquals(90.0, command.getInitialDirectionDeg());
        assertEquals(50.0, command.getInitialSpeed());
        assertEquals(16.0, command.getRadius());

        verify(vehicleSessionRegistryPort).attach(eq("VH-REG"), any(VehicleSessionChannel.class));

        ArgumentCaptor<DisconnectVehicleSessionCommand> disconnectCaptor =
                ArgumentCaptor.forClass(DisconnectVehicleSessionCommand.class);

        verify(disconnectVehicleSessionUseCase).disconnect(disconnectCaptor.capture());
        assertEquals("VH-REG", disconnectCaptor.getValue().getVehicleId());

        verify(vehicleSessionRegistryPort).detach("VH-REG");
        verifyNoInteractions(updateVehicleStateUseCase);
    }

    @Test
    void handleStateUpdateMessageMapsPayloadAndInvokesUpdateUseCase() throws Exception {
        RegisterVehicleSessionUseCase registerVehicleSessionUseCase = mock(RegisterVehicleSessionUseCase.class);
        DisconnectVehicleSessionUseCase disconnectVehicleSessionUseCase = mock(DisconnectVehicleSessionUseCase.class);
        UpdateVehicleStateUseCase updateVehicleStateUseCase = mock(UpdateVehicleStateUseCase.class);
        VehicleSessionRegistryPort vehicleSessionRegistryPort = mock(VehicleSessionRegistryPort.class);

        VehicleTcpSessionHandler handler = new VehicleTcpSessionHandler(
                objectMapper,
                registerVehicleSessionUseCase,
                disconnectVehicleSessionUseCase,
                updateVehicleStateUseCase,
                vehicleSessionRegistryPort
        );

        VehicleState state = new VehicleState();
        state.setId("VH-STATE");
        state.setX(100.0);
        state.setY(200.0);
        state.setDirectionDeg(45.0);
        state.setSpeed(60.0);
        state.setRadius(16.0);
        state.setStatus(VehicleStatus.ACTIVE);
        state.setCurrentAction(AvoidanceAction.TURN_LEFT);
        state.setRiskLevel(RiskLevel.MEDIUM);
        state.setCurrentRiskScore(0.55);

        WireMessage updateMessage = new WireMessage(MessageType.STATE_UPDATE, state);

        Socket socket = mockSocketWithInputLines(
                objectMapper.writeValueAsString(updateMessage)
        );

        handler.handle(socket);

        ArgumentCaptor<UpdateVehicleStateCommand> updateCaptor =
                ArgumentCaptor.forClass(UpdateVehicleStateCommand.class);

        verify(updateVehicleStateUseCase).updateState(updateCaptor.capture());
        UpdateVehicleStateCommand command = updateCaptor.getValue();

        assertEquals("VH-STATE", command.getVehicleId());
        assertEquals(100.0, command.getX());
        assertEquals(200.0, command.getY());
        assertEquals(45.0, command.getDirectionDeg());
        assertEquals(60.0, command.getSpeed());
        assertEquals(16.0, command.getRadius());
        assertEquals(VehicleStatus.ACTIVE, command.getStatus());
        assertEquals(AvoidanceAction.TURN_LEFT, command.getCurrentAction());
        assertEquals(RiskLevel.MEDIUM, command.getRiskLevel());
        assertEquals(0.55, command.getCurrentRiskScore());

        ArgumentCaptor<DisconnectVehicleSessionCommand> disconnectCaptor =
                ArgumentCaptor.forClass(DisconnectVehicleSessionCommand.class);

        verify(disconnectVehicleSessionUseCase).disconnect(disconnectCaptor.capture());
        assertEquals("VH-STATE", disconnectCaptor.getValue().getVehicleId());

        verify(vehicleSessionRegistryPort).detach("VH-STATE");
        verifyNoInteractions(registerVehicleSessionUseCase);
    }

    @Test
    void handleDisconnectMessageResolvesVehicleIdAndCleansUp() throws Exception {
        RegisterVehicleSessionUseCase registerVehicleSessionUseCase = mock(RegisterVehicleSessionUseCase.class);
        DisconnectVehicleSessionUseCase disconnectVehicleSessionUseCase = mock(DisconnectVehicleSessionUseCase.class);
        UpdateVehicleStateUseCase updateVehicleStateUseCase = mock(UpdateVehicleStateUseCase.class);
        VehicleSessionRegistryPort vehicleSessionRegistryPort = mock(VehicleSessionRegistryPort.class);

        VehicleTcpSessionHandler handler = new VehicleTcpSessionHandler(
                objectMapper,
                registerVehicleSessionUseCase,
                disconnectVehicleSessionUseCase,
                updateVehicleStateUseCase,
                vehicleSessionRegistryPort
        );

        WireMessage disconnectMessage = new WireMessage(
                MessageType.DISCONNECT,
                java.util.Map.of("vehicleId", "VH-DISC")
        );

        Socket socket = mockSocketWithInputLines(
                objectMapper.writeValueAsString(disconnectMessage)
        );

        handler.handle(socket);

        ArgumentCaptor<DisconnectVehicleSessionCommand> disconnectCaptor =
                ArgumentCaptor.forClass(DisconnectVehicleSessionCommand.class);

        verify(disconnectVehicleSessionUseCase).disconnect(disconnectCaptor.capture());
        assertEquals("VH-DISC", disconnectCaptor.getValue().getVehicleId());

        verify(vehicleSessionRegistryPort).detach("VH-DISC");
        verifyNoInteractions(registerVehicleSessionUseCase, updateVehicleStateUseCase);
    }

    @Test
    void handleRegisterFailureDetachesSessionImmediately() throws Exception {
        RegisterVehicleSessionUseCase registerVehicleSessionUseCase = mock(RegisterVehicleSessionUseCase.class);
        doThrow(new IOException("ack failed"))
                .when(registerVehicleSessionUseCase)
                .register(any(RegisterVehicleSessionCommand.class));

        DisconnectVehicleSessionUseCase disconnectVehicleSessionUseCase = mock(DisconnectVehicleSessionUseCase.class);
        UpdateVehicleStateUseCase updateVehicleStateUseCase = mock(UpdateVehicleStateUseCase.class);
        VehicleSessionRegistryPort vehicleSessionRegistryPort = mock(VehicleSessionRegistryPort.class);

        VehicleTcpSessionHandler handler = new VehicleTcpSessionHandler(
                objectMapper,
                registerVehicleSessionUseCase,
                disconnectVehicleSessionUseCase,
                updateVehicleStateUseCase,
                vehicleSessionRegistryPort
        );

        RegisterVehicleRequest request = new RegisterVehicleRequest();
        request.setVehicleId("VH-FAIL");
        request.setInitialX(1.0);
        request.setInitialY(2.0);
        request.setInitialDirectionDeg(0.0);
        request.setInitialSpeed(10.0);
        request.setRadius(16.0);

        WireMessage registerMessage = new WireMessage(MessageType.REGISTER, request);

        Socket socket = mockSocketWithInputLines(
                objectMapper.writeValueAsString(registerMessage)
        );

        handler.handle(socket);

        verify(vehicleSessionRegistryPort).attach(eq("VH-FAIL"), any(VehicleSessionChannel.class));
        verify(vehicleSessionRegistryPort).detach("VH-FAIL");

        // vehicleId is not resolved into the outer finally block because register failed before return
        verifyNoInteractions(disconnectVehicleSessionUseCase);
        verifyNoInteractions(updateVehicleStateUseCase);
    }

    private Socket mockSocketWithInputLines(String... lines) throws IOException {
        String input = String.join("\n", lines) + "\n";
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(inputStream);
        when(socket.getOutputStream()).thenReturn(outputStream);

        return socket;
    }
}