package com.example.konecranes.vehicle;

import java.util.HashMap;
import java.util.Map;

public class VehicleProcessConfig {
    private String vehicleId;
    private String gatewayHost;
    private int gatewayPort;
    private double worldWidth;
    private double worldHeight;
    private double initialX;
    private double initialY;
    private double initialDirectionDeg;
    private double initialSpeed;
    private long tickMillis;
    private double radius = 16.0;

    public static VehicleProcessConfig fromArgs(String[] args) {
        Map<String, String> values = new HashMap<>();
        for (String arg : args) {
            if (arg.startsWith("--") && arg.contains("=")) {
                String[] parts = arg.substring(2).split("=", 2);
                values.put(parts[0], parts[1]);
            }
        }
        VehicleProcessConfig config = new VehicleProcessConfig();
        config.vehicleId = values.get("vehicleId");
        config.gatewayHost = values.getOrDefault("gatewayHost", "127.0.0.1");
        config.gatewayPort = Integer.parseInt(values.getOrDefault("gatewayPort", "9090"));
        config.worldWidth = Double.parseDouble(values.getOrDefault("worldWidth", "1000"));
        config.worldHeight = Double.parseDouble(values.getOrDefault("worldHeight", "700"));
        config.initialX = Double.parseDouble(values.getOrDefault("initialX", "100"));
        config.initialY = Double.parseDouble(values.getOrDefault("initialY", "100"));
        config.initialDirectionDeg = Double.parseDouble(values.getOrDefault("initialDirectionDeg", "0"));
        config.initialSpeed = Double.parseDouble(values.getOrDefault("initialSpeed", "50"));
        config.tickMillis = Long.parseLong(values.getOrDefault("tickMillis", "100"));
        return config;
    }

    public String getVehicleId() { return vehicleId; }
    public String getGatewayHost() { return gatewayHost; }
    public int getGatewayPort() { return gatewayPort; }
    public double getWorldWidth() { return worldWidth; }
    public double getWorldHeight() { return worldHeight; }
    public double getInitialX() { return initialX; }
    public double getInitialY() { return initialY; }
    public double getInitialDirectionDeg() { return initialDirectionDeg; }
    public double getInitialSpeed() { return initialSpeed; }
    public long getTickMillis() { return tickMillis; }
    public double getRadius() { return radius; }
}
