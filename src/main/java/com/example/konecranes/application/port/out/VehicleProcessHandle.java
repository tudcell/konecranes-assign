package com.example.konecranes.application.port.out;

/**
 * Handle for a vehicle runtime process started by the application.
 *
 * Wraps process lifecycle operations so the application layer
 * does not depend directly on the JVM Process API.
 */
public interface VehicleProcessHandle {

    /**
     * Checks whether the process is still running.
     *
     * @return true when the process is alive
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
     * Waits for the process to exit or for the timeout to expire.
     *
     * @param timeoutMillis maximum wait time in milliseconds
     * @return true when the process exited before the timeout
     * @throws InterruptedException when the waiting thread is interrupted
     */
    boolean waitFor(long timeoutMillis) throws InterruptedException;
}