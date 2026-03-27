package com.example.konecranes;

import com.example.konecranes.vehicle.VehicleProcessMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import java.util.Arrays;

/**
 * Main application entry point.
 *
 * Starts either:
 * - coordinator mode
 * - vehicle process mode
 *
 * Vehicle mode is selected when the argument --mode=vehicle is present.
 */
public class ApplicationLauncher {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationLauncher.class);
    private static final String VEHICLE_MODE_ARGUMENT = "--mode=vehicle";

    /**
     * Launches the application in coordinator mode by default,
     * or delegates to the standalone vehicle process entry point
     * when vehicle mode is requested.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        logger.info("Application launcher started with args={}", Arrays.toString(args));

        boolean isVehicleMode = Arrays.asList(args).contains(VEHICLE_MODE_ARGUMENT);
        if (isVehicleMode) {
            String[] vehicleProcessArgs = Arrays.stream(args)
                    .filter(arg -> !VEHICLE_MODE_ARGUMENT.equals(arg))
                    .toArray(String[]::new);

            logger.info("Launching vehicle process mode with args={}", Arrays.toString(vehicleProcessArgs));
            VehicleProcessMain.main(vehicleProcessArgs);
            return;
        }

        logger.info("Launching coordinator mode");
        SpringApplication.run(KonecranesCoordinatorApplication.class, args);
    }
}