package com.example.konecranes.adapter.in.tcp;

import com.example.konecranes.application.port.out.VehicleSessionRegistryPort;
import com.example.konecranes.config.SimulationProperties;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class VehicleTcpServerTest {

    @Test
    void startThrowsIllegalStateExceptionWhenServerSocketCannotBeCreated() throws IOException {
        SimulationProperties simulationProperties = new SimulationProperties();
        simulationProperties.getGateway().setPort(9090);

        VehicleTcpSessionHandler vehicleTcpSessionHandler = mock(VehicleTcpSessionHandler.class);
        VehicleSessionRegistryPort vehicleSessionRegistryPort = mock(VehicleSessionRegistryPort.class);

        VehicleTcpServer vehicleTcpServer = spy(
                new VehicleTcpServer(
                        simulationProperties,
                        vehicleTcpSessionHandler,
                        vehicleSessionRegistryPort
                )
        );

        doThrow(new IOException("bind failed"))
                .when(vehicleTcpServer)
                .createServerSocket(9090);

        assertThrows(IllegalStateException.class, vehicleTcpServer::start);

        verify(vehicleTcpServer).createServerSocket(9090);
        verifyNoInteractions(vehicleTcpSessionHandler, vehicleSessionRegistryPort);
    }

    @Test
    void stopClosesSocketAndDetachesAllSessionsAfterStart() throws Exception {
        SimulationProperties simulationProperties = new SimulationProperties();
        simulationProperties.getGateway().setPort(9090);

        VehicleTcpSessionHandler vehicleTcpSessionHandler = mock(VehicleTcpSessionHandler.class);
        VehicleSessionRegistryPort vehicleSessionRegistryPort = mock(VehicleSessionRegistryPort.class);

        VehicleTcpServer vehicleTcpServer = spy(
                new VehicleTcpServer(
                        simulationProperties,
                        vehicleTcpSessionHandler,
                        vehicleSessionRegistryPort
                )
        );

        ServerSocket serverSocket = mock(ServerSocket.class);
        when(serverSocket.isClosed()).thenReturn(false);

        // Make the accept loop exit quickly after start.
        when(serverSocket.accept()).thenThrow(new IOException("stop accept loop"));

        doReturn(serverSocket)
                .when(vehicleTcpServer)
                .createServerSocket(9090);

        vehicleTcpServer.start();
        vehicleTcpServer.stop();

        verify(vehicleTcpServer).createServerSocket(9090);
        verify(vehicleSessionRegistryPort).detachAll();
        verify(serverSocket).close();
    }

    @Test
    void stopDoesNothingWhenServerWasNeverStarted() {
        SimulationProperties simulationProperties = new SimulationProperties();
        simulationProperties.getGateway().setPort(9090);

        VehicleTcpSessionHandler vehicleTcpSessionHandler = mock(VehicleTcpSessionHandler.class);
        VehicleSessionRegistryPort vehicleSessionRegistryPort = mock(VehicleSessionRegistryPort.class);

        VehicleTcpServer vehicleTcpServer = new VehicleTcpServer(
                simulationProperties,
                vehicleTcpSessionHandler,
                vehicleSessionRegistryPort
        );

        vehicleTcpServer.stop();

        verifyNoInteractions(vehicleTcpSessionHandler, vehicleSessionRegistryPort);
    }
}