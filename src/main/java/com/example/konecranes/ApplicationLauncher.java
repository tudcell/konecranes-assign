package com.example.konecranes;

import com.example.konecranes.vehicle.VehicleProcessMain;
import org.springframework.boot.SpringApplication;

import java.util.Arrays;

public class ApplicationLauncher {

    public static void main(String[] args) {
        boolean vehicleMode = Arrays.stream(args).anyMatch("--mode=vehicle"::equals);
        if (vehicleMode) {
            String[] filteredArgs = Arrays.stream(args)
                    .filter(arg -> !"--mode=vehicle".equals(arg))
                    .toArray(String[]::new);
            VehicleProcessMain.main(filteredArgs);
            return;
        }
        SpringApplication.run(KonecranesCoordinatorApplication.class, args);
    }
}
