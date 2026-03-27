package com.example.konecranes.adapter.out.tcp;

import com.example.konecranes.messaging.WireMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.io.BufferedWriter;
import java.io.IOException;
import static org.mockito.Mockito.*;

class TcpVehicleSessionChannelTest {
    @Test
    void send_writesJsonAndFlushes() throws IOException {
        BufferedWriter writer = mock(BufferedWriter.class);
        ObjectMapper objectMapper = new ObjectMapper();
        TcpVehicleSessionChannel channel = new TcpVehicleSessionChannel(writer, objectMapper);
        WireMessage message = new WireMessage();
        channel.send(message);
        verify(writer).write(anyString());
        verify(writer).newLine();
        verify(writer).flush();
    }

    @Test
    void close_closesWriter() throws IOException {
        BufferedWriter writer = mock(BufferedWriter.class);
        ObjectMapper objectMapper = new ObjectMapper();
        TcpVehicleSessionChannel channel = new TcpVehicleSessionChannel(writer, objectMapper);
        channel.close();
        verify(writer).close();
    }
}

