package com.example.konecranes.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "simulation")
public class SimulationProperties {

    private final World world = new World();
    private final Gateway gateway = new Gateway();
    private final Vehicle vehicle = new Vehicle();

    public World getWorld() {
        return world;
    }

    public Gateway getGateway() {
        return gateway;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public static class World {
        private double width;
        private double height;

        public double getWidth() {
            return width;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }
    }

    public static class Gateway {
        private String host;
        private int port;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    public static class Vehicle {
        private String jarPath;
        private double defaultSpeed;
        private long tickMillis;

        public String getJarPath() {
            return jarPath;
        }

        public void setJarPath(String jarPath) {
            this.jarPath = jarPath;
        }

        public double getDefaultSpeed() {
            return defaultSpeed;
        }

        public void setDefaultSpeed(double defaultSpeed) {
            this.defaultSpeed = defaultSpeed;
        }

        public long getTickMillis() {
            return tickMillis;
        }

        public void setTickMillis(long tickMillis) {
            this.tickMillis = tickMillis;
        }
    }
}
