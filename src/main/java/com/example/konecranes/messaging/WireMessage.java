package com.example.konecranes.messaging;

import lombok.Getter;
import lombok.Setter;

/**
 * Generic line-delimited JSON envelope exchanged over TCP.
 */
@Setter
@Getter
public class WireMessage {

    private MessageType type;

    private Object payload;

    public WireMessage() {
    }

    public WireMessage(MessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

}
