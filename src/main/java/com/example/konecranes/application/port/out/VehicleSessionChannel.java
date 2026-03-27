package com.example.konecranes.application.port.out;

import com.example.konecranes.messaging.WireMessage;

import java.io.IOException;

/**
 * Transport-agnostic channel for one active vehicle session.
 */
public interface VehicleSessionChannel {

    /**
     * Sends one wire message to the connected vehicle.
     *
     * @param message message to send
     * @throws IOException when the underlying transport fails
     */
    void send(WireMessage message) throws IOException;

    /**
     * Closes the underlying session resources.
     *
     * @throws IOException when close fails
     */
    void close() throws IOException;
}