package com.example.konecranes.application.port.in;

/**
 * Use case for managing live simulation snapshot subscriptions.
 *
 * Allows clients to subscribe to simulation updates
 * and later remove their subscription.
 */
public interface SimulationStreamUseCase {

    /**
     * Registers one listener for snapshot events.
     *
     * @param listener listener invoked for each emitted snapshot
     * @return subscription id used for later unsubscription
     */
    String subscribe(SimulationSnapshotListener listener);

    /**
     * Removes a previously registered snapshot subscription.
     *
     * @param subscriptionId subscription id returned by subscribe
     */
    void unsubscribe(String subscriptionId);
}