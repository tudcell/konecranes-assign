package com.example.konecranes.messaging;

/**
 * Generic line-delimited JSON envelope exchanged over TCP.
 */
public class WireMessage {
    private MessageType type;
    private Object payload;

    public WireMessage() {
    }

    /**
     * @param type message discriminator
     * @param payload message body matching the message type
     */
    public WireMessage(MessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    /** @return message discriminator */
    public MessageType getType() {
        return type;
    }

    /** @param type message discriminator */
    public void setType(MessageType type) {
        this.type = type;
    }

    /** @return message payload */
    public Object getPayload() {
        return payload;
    }

    /** @param payload message payload */
    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
