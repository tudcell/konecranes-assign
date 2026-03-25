package com.example.konecranes.application.port.in;

/**
 * Manages live simulation snapshot subscriptions.
 */
public interface SimulationStreamUseCase {

    /**
     * Registers one listener for snapshot events.
     *
     * @param listener callback invoked for every emitted snapshot
     * @return subscription id used to later unsubscribe
     */
    String subscribe(SimulationSnapshotListener listener);

    /**
     * Removes an existing snapshot subscription.
     *
     * @param subscriptionId id returned by {@link #subscribe(SimulationSnapshotListener)}
     */
    void unsubscribe(String subscriptionId);
}
