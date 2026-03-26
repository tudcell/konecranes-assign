package com.example.konecranes.adapter.in.tcp;

import com.example.konecranes.adapter.out.tcp.SessionConnectionRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import com.example.konecranes.config.SimulationProperties;

import static org.mockito.Mockito.*;

class VehicleGatewayServerTest {
    @Test
    void startsAndAcceptsConnection() throws Exception {
        // Mock dependencies
        SimulationProperties properties = Mockito.mock(SimulationProperties.class);
        SimulationProperties.Gateway gateway = new SimulationProperties.Gateway();
        gateway.setPort(12345);
        when(properties.getGateway()).thenReturn(gateway);
        VehicleSessionHandler handler = mock(VehicleSessionHandler.class);
        SessionConnectionRegistry registry = mock(SessionConnectionRegistry.class);
        ServerSocket serverSocket = mock(ServerSocket.class);
        Socket socket = mock(Socket.class);
        when(serverSocket.accept()).thenReturn(socket).thenThrow(new IOException("Stop"));

        VehicleGatewayServer server = new VehicleGatewayServer(properties, handler, registry);
        server.setServerSocket(serverSocket); // Use the new setter
        // Set running to true for the accept loop
        java.lang.reflect.Field runningField = VehicleGatewayServer.class.getDeclaredField("running");
        runningField.setAccessible(true);
        runningField.set(server, new java.util.concurrent.atomic.AtomicBoolean(true));

        Thread serverThread = new Thread(() -> {
            try {
                java.lang.reflect.Method acceptLoop = VehicleGatewayServer.class.getDeclaredMethod("acceptLoop");
                acceptLoop.setAccessible(true);
                acceptLoop.invoke(server);
            } catch (Exception ignored) {}
        });
        serverThread.start();
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        // Stop the loop after the first exception to avoid repeated error logs
        ((java.util.concurrent.atomic.AtomicBoolean) runningField.get(server)).set(false);
        serverThread.join(200); // Wait for thread to finish
        verify(handler, atLeastOnce()).handle(any(Socket.class));
    }
}
