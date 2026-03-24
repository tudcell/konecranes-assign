package com.example.konecranes.application.port.out;

public interface VehicleProcessHandle {
    boolean isAlive();

    void destroy();

    void destroyForcibly();

    boolean waitFor(long timeoutMillis) throws InterruptedException;
}


