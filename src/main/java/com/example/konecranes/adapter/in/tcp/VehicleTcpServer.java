package com.example.konecranes.adapter.in.tcp;

import com.example.konecranes.application.port.out.VehicleSessionRegistryPort;
import com.example.konecranes.config.SimulationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
 * TCP server that accepts vehicle connections and dispatches each session to a handler.
 */
@Component
public class VehicleTcpServer {

    private static final Logger logger = LoggerFactory.getLogger(VehicleTcpServer.class);
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 3L;

    private final int gatewayPort;
    private final VehicleTcpSessionHandler sessionHandler;
    private final VehicleSessionRegistryPort sessionRegistryPort;
    private final ExecutorService acceptorPool = Executors.newSingleThreadExecutor();
    private final ExecutorService clientPool = Executors.newCachedThreadPool();
    private final AtomicBoolean running = new AtomicBoolean(false);

    private ServerSocket serverSocket;

    public VehicleTcpServer(SimulationProperties properties,
                            VehicleTcpSessionHandler sessionHandler,
                            VehicleSessionRegistryPort sessionRegistryPort) {
        this.gatewayPort = properties.getGateway().getPort();
        this.sessionHandler = sessionHandler;
        this.sessionRegistryPort = sessionRegistryPort;
    }

    @PostConstruct
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        try {
            serverSocket = new ServerSocket(gatewayPort);
        } catch (IOException ex) {
            running.set(false);
            throw new IllegalStateException("Failed to open vehicle TCP server on port " + gatewayPort, ex);
        }

        logger.info("Vehicle TCP server started on port {}", gatewayPort);
        acceptorPool.submit(this::acceptLoop);
    }

    private void acceptLoop() {
        while (running.get()) {
            try {
                Socket clientSocket = serverSocket.accept();
                clientPool.submit(() -> sessionHandler.handle(clientSocket));
            } catch (IOException ex) {
                if (running.get()) {
                    logger.error("Vehicle TCP accept loop failed", ex);
                }
                break;
            }
        }
    }

    @PreDestroy
    public void stop() {
        if (!running.getAndSet(false)) {
            return;
        }

        closeServerSocket();
        sessionRegistryPort.detachAll();
        shutdownExecutor(acceptorPool, "vehicle-tcp-acceptor");
        shutdownExecutor(clientPool, "vehicle-tcp-client");
        logger.info("Vehicle TCP server stopped");
    }

    private void closeServerSocket() {
        if (serverSocket == null || serverSocket.isClosed()) {
            return;
        }
        try {
            serverSocket.close();
        } catch (IOException ex) {
            logger.warn("Failed to close vehicle TCP server socket", ex);
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

    // Package-private setter for tests
    void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }
}