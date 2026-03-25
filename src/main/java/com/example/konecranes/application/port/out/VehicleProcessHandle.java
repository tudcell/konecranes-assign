package com.example.konecranes.application.port.out;

/**
 * Abstraction over an OS process started for a vehicle runtime.
 */
public interface VehicleProcessHandle {

    /**
     * @return true when process is still running
     */
    boolean isAlive();

    /**
     * Requests graceful process termination.
     */
    void destroy();

    /**
     * Requests forced process termination.
     */
    void destroyForcibly();

    /**
     * Waits until process exits or timeout elapses.
     *
     * @param timeoutMillis wait timeout in milliseconds
     * @return true when process exited before timeout
     * @throws InterruptedException when waiting thread is interrupted
     */
    boolean waitFor(long timeoutMillis) throws InterruptedException;
}
