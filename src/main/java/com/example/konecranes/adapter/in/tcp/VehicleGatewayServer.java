package com.example.konecranes.adapter.in.tcp;

import com.example.konecranes.adapter.out.tcp.VehicleSessionConnectionRegistry;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TCP inbound adapter that accepts vehicle connections and dispatches sessions.
 */
@Component
public class VehicleGatewayServer {

    private static final Logger logger = LoggerFactory.getLogger(VehicleGatewayServer.class);
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 3L;

    private final int gatewayPort;
    private final VehicleSessionHandler sessionHandler;
    private final VehicleSessionConnectionRegistry sessionConnectionRegistry;
    private final ExecutorService acceptorPool = Executors.newSingleThreadExecutor();
    private final ExecutorService clientPool = Executors.newCachedThreadPool();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ServerSocket serverSocket;

    public VehicleGatewayServer(com.example.konecranes.config.SimulationProperties properties,
                                VehicleSessionHandler sessionHandler,
                                VehicleSessionConnectionRegistry sessionConnectionRegistry) {
        this.gatewayPort = properties.getGateway().getPort();
        this.sessionHandler = sessionHandler;
        this.sessionConnectionRegistry = sessionConnectionRegistry;
    }

    /**
     * Opens the server socket and starts accept loop if not already running.
     */
    @PostConstruct
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        try {
            serverSocket = new ServerSocket(gatewayPort);
        } catch (IOException ex) {
            running.set(false);
            throw new IllegalStateException("Failed to open vehicle gateway on port " + gatewayPort, ex);
        }

        logger.info("Vehicle gateway started on port {}", gatewayPort);
        acceptorPool.submit(this::acceptLoop);
    }

    private void acceptLoop() {
        while (running.get()) {
            try {
                Socket clientSocket = serverSocket.accept();
                clientPool.submit(() -> sessionHandler.handle(clientSocket));
            } catch (IOException ex) {
                if (running.get()) {
                    logger.error("Gateway accept loop failed", ex);
                }
                // Break the loop after IOException to avoid repeated error logs
                break;
            }
        }
    }

    /**
     * Stops accept loop, detaches active sessions, and shuts down executors.
     */
    @PreDestroy
    public void stop() {
        if (!running.getAndSet(false)) {
            return;
        }
        closeServerSocket();
        sessionConnectionRegistry.detachAll();
        shutdownExecutor(acceptorPool, "gateway-acceptor");
        shutdownExecutor(clientPool, "gateway-client");
        logger.info("Vehicle gateway stopped");
    }

    private void closeServerSocket() {
        if (serverSocket == null || serverSocket.isClosed()) {
            return;
        }
        try {
            serverSocket.close();
        } catch (IOException ex) {
            logger.warn("Failed to close vehicle gateway socket", ex);
        }
    }

    private void shutdownExecutor(ExecutorService executor, String executorName) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                logger.warn("{} executor did not stop in {}s; forcing shutdown", executorName, SHUTDOWN_TIMEOUT_SECONDS);
                executor.shutdownNow();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    // Package-private setter for testability
    void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }
}
