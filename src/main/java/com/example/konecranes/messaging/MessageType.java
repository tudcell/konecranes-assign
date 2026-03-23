package com.example.konecranes.messaging;

public enum MessageType {
    REGISTER,
    REGISTER_ACK,
    STATE_UPDATE,
    ENVIRONMENT_UPDATE,
    CONTROL_COMMAND,
    HEARTBEAT,
    DISCONNECT
}
