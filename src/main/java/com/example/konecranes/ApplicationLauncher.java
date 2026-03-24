package com.example.konecranes;

import com.example.konecranes.vehicle.VehicleProcessMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import java.util.Arrays;

public class ApplicationLauncher {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationLauncher.class);
    public static void main(String[] args) {

        logger.info("Application launcher started with args={}", Arrays.toString(args));

        boolean vehicleMode = Arrays.asList(args).contains("--mode=vehicle");
        if (vehicleMode) {
            String[] filteredArgs = Arrays.stream(args)
                    .filter(arg -> !"--mode=vehicle".equals(arg))
                    .toArray(String[]::new);

            logger.info("Launching vehicle process mode with args={}", Arrays.toString(filteredArgs));
            VehicleProcessMain.main(filteredArgs);
            return;
        }

        logger.info("Launching coordinator mode");
        SpringApplication.run(KonecranesCoordinatorApplication.class, args);
    }
}
