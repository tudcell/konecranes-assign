package com.example.konecranes.messaging.gateway;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class VehicleGatewayServer {

    private final VehicleSessionHandler sessionHandler;
    private final ExecutorService acceptorPool = Executors.newSingleThreadExecutor();
    private final ExecutorService clientPool = Executors.newCachedThreadPool();
    private volatile boolean running = true;
    private ServerSocket serverSocket;

    public VehicleGatewayServer(com.example.konecranes.config.SimulationProperties properties,
                                VehicleSessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
        try {
            this.serverSocket = new ServerSocket(properties.getGateway().getPort());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to open vehicle gateway", e);
        }
    }

    @PostConstruct
    public void start() {
        acceptorPool.submit(() -> {
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientPool.submit(() -> sessionHandler.handle(clientSocket));
                } catch (IOException ex) {
                    if (running) {
                        throw new IllegalStateException("Gateway accept loop failed", ex);
                    }
                }
            }
        });
    }

    @PreDestroy
    public void stop() throws IOException {
        running = false;
        serverSocket.close();
        acceptorPool.shutdownNow();
        clientPool.shutdownNow();
    }
}

