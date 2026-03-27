package com.example.konecranes.adapter.out.tcp;

import com.example.konecranes.application.port.out.VehicleSessionChannel;
import com.example.konecranes.messaging.WireMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * TCP implementation of a vehicle session channel.
 *
 * Serializes wire messages as JSON and writes them
 * as line-delimited messages to the socket writer.
 */
public class TcpVehicleSessionChannel implements VehicleSessionChannel {

    private final BufferedWriter writer;
    private final ObjectMapper objectMapper;

    public TcpVehicleSessionChannel(BufferedWriter writer, ObjectMapper objectMapper) {
        this.writer = writer;
        this.objectMapper = objectMapper;
    }

    /**
     * Sends one wire message through the TCP session.
     *
     * Message serialization and writing are synchronized on the writer
     * so concurrent sends do not interleave on the output stream.
     *
     * @param message wire message to send
     * @throws IOException when serialization or socket writing fails
     */
    @Override
    public void send(WireMessage message) throws IOException {
        synchronized (writer) {
            writer.write(objectMapper.writeValueAsString(message));
            writer.newLine();
            writer.flush();
        }
    }

    /**
     * Closes the underlying TCP writer for this session.
     *
     * @throws IOException when close fails
     */
    @Override
    public void close() throws IOException {
        writer.close();
    }
}