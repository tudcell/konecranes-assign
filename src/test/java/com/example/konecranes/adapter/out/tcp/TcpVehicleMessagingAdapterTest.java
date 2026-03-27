package com.example.konecranes.adapter.out.tcp;

import com.example.konecranes.application.port.out.VehicleSessionChannel;
import com.example.konecranes.messaging.RegisterVehicleAck;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TcpVehicleMessagingAdapterTest {
    @Test
    void attachAndSendAck_callsChannelSend() throws IOException {
        TcpVehicleMessagingAdapter adapter = new TcpVehicleMessagingAdapter();
        VehicleSessionChannel channel = mock(VehicleSessionChannel.class);
        adapter.attach("VH-1", channel);
        RegisterVehicleAck ack = new RegisterVehicleAck();
        adapter.sendAck("VH-1", ack);
        verify(channel).send(any());
    }

    @Test
    void detach_closesChannel() throws IOException {
        TcpVehicleMessagingAdapter adapter = new TcpVehicleMessagingAdapter();
        VehicleSessionChannel channel = mock(VehicleSessionChannel.class);
        adapter.attach("VH-1", channel);
        adapter.detach("VH-1");
        verify(channel).close();
    }

    @Test
    void detachAll_closesAllChannels() throws IOException {
        TcpVehicleMessagingAdapter adapter = new TcpVehicleMessagingAdapter();
        VehicleSessionChannel c1 = mock(VehicleSessionChannel.class);
        VehicleSessionChannel c2 = mock(VehicleSessionChannel.class);
        adapter.attach("VH-1", c1);
        adapter.attach("VH-2", c2);
        adapter.detachAll();
        verify(c1).close();
        verify(c2).close();
    }

    @Test
    void sendThrowsIfNoChannel() {
        TcpVehicleMessagingAdapter adapter = new TcpVehicleMessagingAdapter();
        assertThrows(IOException.class, () -> adapter.sendAck("VH-404", new RegisterVehicleAck()));
    }
}

