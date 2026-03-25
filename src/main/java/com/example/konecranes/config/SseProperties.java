package com.example.konecranes.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for SSE (Server-Sent Events) behavior.
 * 
 * Note: The Spring async request timeout (spring.mvc.async.request-timeout) must be set high enough
 * to prevent premature termination of long-lived SSE connections.
 * When SseEmitter timeout is 0L (never timeout), the connection persists until client disconnects.
 */
@Component
@ConfigurationProperties(prefix = "simulation.sse")
public class SseProperties {
    
    /**
     * SSE emitter timeout in milliseconds.
     * 0L means never timeout - connection stays open until client disconnects.
     * Default: 0 (no timeout)
     */
    private long emitterTimeoutMillis = 0L;

    /** @return SSE emitter timeout in milliseconds */
    public long getEmitterTimeoutMillis() {
        return emitterTimeoutMillis;
    }

    /** @param emitterTimeoutMillis SSE emitter timeout in milliseconds */
    public void setEmitterTimeoutMillis(long emitterTimeoutMillis) {
        this.emitterTimeoutMillis = emitterTimeoutMillis;
    }
}


