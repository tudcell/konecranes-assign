package com.example.konecranes.adapter.out.tcp;

import com.example.konecranes.application.port.out.VehicleSessionChannel;
import com.example.konecranes.messaging.WireMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * TCP-backed vehicle session channel that writes line-delimited JSON messages.
 */
public class TcpVehicleSessionChannel implements VehicleSessionChannel {

    private final BufferedWriter writer;
    private final ObjectMapper objectMapper;

    public TcpVehicleSessionChannel(BufferedWriter writer, ObjectMapper objectMapper) {
        this.writer = writer;
        this.objectMapper = objectMapper;
    }

    @Override
    public void send(WireMessage message) throws IOException {
        synchronized (writer) {
            writer.write(objectMapper.writeValueAsString(message));
            writer.newLine();
            writer.flush();
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}