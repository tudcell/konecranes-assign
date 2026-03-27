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
 * TCP server responsible for accepting vehicle connections
 * and delegating each connection to the TCP session handler.
 */
@Component
public class VehicleTcpServer {

    private static final Logger logger = LoggerFactory.getLogger(VehicleTcpServer.class);
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 3L;

    private final int gatewayPort;
    private final VehicleTcpSessionHandler vehicleTcpSessionHandler;
    private final VehicleSessionRegistryPort vehicleSessionRegistryPort;
    private final ExecutorService acceptorExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService clientExecutor = Executors.newCachedThreadPool();
    private final AtomicBoolean running = new AtomicBoolean(false);

    private ServerSocket serverSocket;

    public VehicleTcpServer(SimulationProperties properties,
                            VehicleTcpSessionHandler vehicleTcpSessionHandler,
                            VehicleSessionRegistryPort vehicleSessionRegistryPort) {
        this.gatewayPort = properties.getGateway().getPort();
        this.vehicleTcpSessionHandler = vehicleTcpSessionHandler;
        this.vehicleSessionRegistryPort = vehicleSessionRegistryPort;
    }

    /**
     * Starts the TCP server and launches the accept loop.
     *
     * If the server is already running, this method does nothing.
     */
    @PostConstruct
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        try {
            serverSocket = createServerSocket(gatewayPort);
        } catch (IOException ex) {
            running.set(false);
            throw new IllegalStateException("Failed to open vehicle TCP server on port " + gatewayPort, ex);
        }

        logger.info("Vehicle TCP server started on port {}", gatewayPort);
        acceptorExecutor.submit(this::acceptLoop);
    }

    /**
     * Factory method used to create the main server socket.
     *
     * Extracted for easier testing so socket creation can be stubbed.
     *
     * @param port TCP port to bind
     * @return created server socket
     * @throws IOException when binding fails
     */
    ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocket(port);
    }

    /**
     * Accepts incoming sockets and hands each connection
     * to the vehicle TCP session handler.
     *
     * Stops when the server is no longer running or
     * when an I/O failure occurs on the server socket.
     */
    private void acceptLoop() {
        while (running.get()) {
            try {
                Socket clientSocket = serverSocket.accept();
                clientExecutor.submit(() -> vehicleTcpSessionHandler.handle(clientSocket));
            } catch (IOException ex) {
                if (running.get()) {
                    logger.error("Vehicle TCP accept loop failed", ex);
                }
                break;
            }
        }
    }

    /**
     * Stops the TCP server and cleans up active resources.
     *
     * This closes the server socket, detaches all active vehicle sessions,
     * and shuts down both the acceptor and client executors.
     */
    @PreDestroy
    public void stop() {
        if (!running.getAndSet(false)) {
            return;
        }

        closeServerSocket();
        vehicleSessionRegistryPort.detachAll();
        shutdownExecutor(acceptorExecutor, "vehicle-tcp-acceptor");
        shutdownExecutor(clientExecutor, "vehicle-tcp-client");
        logger.info("Vehicle TCP server stopped");
    }

    /**
     * Closes the main server socket if it is still open.
     */
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

    /**
     * Shuts down one executor gracefully and forces shutdown
     * if it does not terminate within the configured timeout.
     *
     * @param executor executor to stop
     * @param executorName log-friendly executor name
     */
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

    // Package-private setter used by tests.
    void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }
}