package com.example.konecranes.messaging;

import com.example.konecranes.model.MessageType;


public class WireMessage {
    private MessageType type;
    private Object payload;

    public WireMessage() {
    }

    public WireMessage(MessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
