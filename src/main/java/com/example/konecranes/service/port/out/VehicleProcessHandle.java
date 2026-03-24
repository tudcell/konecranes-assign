package com.example.konecranes.service.port.out;

public interface VehicleProcessHandle {
    boolean isAlive();

    void destroy();

    void destroyForcibly();

    boolean waitFor(long timeoutMillis) throws InterruptedException;
}

