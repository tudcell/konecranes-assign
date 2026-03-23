package com.example.konecranes.service;

import com.example.konecranes.config.SimulationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class VehicleSpawnerService {

    private final SimulationProperties properties;

    public VehicleSpawnerService(SimulationProperties properties) {
        this.properties = properties;
    }

    public List<String> spawn(int count) throws IOException {
        Path jarPath = Path.of(properties.getVehicle().getJarPath()).toAbsolutePath();
        if (!Files.exists(jarPath)) {
            throw new IOException("Built jar not found at " + jarPath + ". Run 'mvn clean package' first.");
        }

        List<String> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String vehicleId = "VH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            ids.add(vehicleId);

            List<String> command = new ArrayList<>();
            command.add(resolveJavaExecutable());
            command.add("-jar");
            command.add(jarPath.toString());
            command.add("--mode=vehicle");
            command.add("--vehicleId=" + vehicleId);
            command.add("--gatewayHost=" + properties.getGateway().getHost());
            command.add("--gatewayPort=" + properties.getGateway().getPort());
            command.add("--worldWidth=" + properties.getWorld().getWidth());
            command.add("--worldHeight=" + properties.getWorld().getHeight());
            command.add("--initialX=" + random(50.0, properties.getWorld().getWidth() - 50.0));
            command.add("--initialY=" + random(50.0, properties.getWorld().getHeight() - 50.0));
            command.add("--initialDirectionDeg=" + random(0.0, 359.0));
            command.add("--initialSpeed=" + properties.getVehicle().getDefaultSpeed());
            command.add("--tickMillis=" + properties.getVehicle().getTickMillis());

            new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .inheritIO()
                    .start();
        }
        return ids;
    }

    private String resolveJavaExecutable() {
        String javaHome = System.getProperty("java.home");
        return Path.of(javaHome, "bin", "java").toString();
    }

    private double random(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
}
