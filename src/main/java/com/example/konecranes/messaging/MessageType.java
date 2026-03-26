package com.example.konecranes.messaging;

/**
 * Wire protocol message types exchanged between coordinator and vehicle processes.
 */
public enum MessageType {
    /** Vehicle asks to establish a session. */
    REGISTER,
    /** Coordinator confirms registration and world metadata. */
    REGISTER_ACK,
    /** Vehicle publishes latest movement/state sample. */
    STATE_UPDATE,
    /** Coordinator sends nearby-vehicle context. */
    ENVIRONMENT_UPDATE,
    /** Coordinator sends user/manual control input. */
    CONTROL_COMMAND,
    /** Reserved keep-alive message type. */
    HEARTBEAT,
    /** Either side requests session termination. */
    DISCONNECT
}

