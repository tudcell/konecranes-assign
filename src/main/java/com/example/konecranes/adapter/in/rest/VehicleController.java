package com.example.konecranes.adapter.in.rest;

import com.example.konecranes.application.port.in.VehicleControlUseCase;
import com.example.konecranes.application.port.in.VehicleSpawnUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

/**
 * REST adapter that exposes vehicle spawn and manual control actions.
 */
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleSpawnUseCase vehicleSpawnerService;
    private final VehicleControlUseCase vehicleCommandService;

    public VehicleController(VehicleSpawnUseCase vehicleSpawnerService,
                             VehicleControlUseCase vehicleCommandService) {
        this.vehicleSpawnerService = vehicleSpawnerService;
        this.vehicleCommandService = vehicleCommandService;
    }

    /**
     * Creates one or more vehicle processes.
     *
     * @param request JSON payload containing vehicle count
     * @return response with created vehicle ids
     * @throws IOException when spawning fails
     */
    @PostMapping("/spawn")
    @ResponseStatus(HttpStatus.CREATED)
    public SpawnResponse spawn(@Valid @RequestBody SpawnRequest request) throws IOException {
        List<String> vehicleIds = vehicleSpawnerService.spawn(request.getCount());
        return new SpawnResponse(vehicleIds);
    }

    /**
     * Applies manual direction override to a vehicle.
     *
     * @param vehicleId target vehicle id
     * @param request payload containing desired heading
     * @throws IOException when command dispatch fails
     */
    @PostMapping("/{vehicleId}/direction")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void overrideDirection(@PathVariable String vehicleId,
                                  @Valid @RequestBody DirectionCommand request) throws IOException {
        vehicleCommandService.overrideDirection(vehicleId, request.getDirectionDeg());
    }

    /**
     * Applies manual speed override to a vehicle.
     *
     * @param vehicleId target vehicle id
     * @param request payload containing desired speed
     * @throws IOException when command dispatch fails
     */
    @PostMapping("/{vehicleId}/speed")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void overrideSpeed(@PathVariable String vehicleId,
                              @Valid @RequestBody SpeedCommand request) throws IOException {
        vehicleCommandService.overrideSpeed(vehicleId, request.getSpeed());
    }

    /**
     * Input payload for spawn requests.
     */
    public static class SpawnRequest {
        @Min(1)
        @Max(25)
        private int count;

        /** @return number of vehicles to spawn */
        public int getCount() {
            return count;
        }

        /** @param count number of vehicles to spawn */
        public void setCount(int count) {
            this.count = count;
        }
    }

    /**
     * Output payload containing created vehicle ids.
     */
    public static class SpawnResponse {
        private final List<String> vehicleIds;

        /**
         * @param vehicleIds spawned vehicle identifiers
         */
        public SpawnResponse(List<String> vehicleIds) {
            this.vehicleIds = vehicleIds;
        }

        /** @return spawned vehicle identifiers */
        public List<String> getVehicleIds() {
            return vehicleIds;
        }
    }

    /**
     * Input payload for direction override.
     */
    public static class DirectionCommand {
        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("359.99")
        private Double directionDeg;

        /** @return desired direction in degrees */
        public Double getDirectionDeg() {
            return directionDeg;
        }

        /** @param directionDeg desired direction in degrees */
        public void setDirectionDeg(Double directionDeg) {
            this.directionDeg = directionDeg;
        }
    }

    /**
     * Input payload for speed override.
     */
    public static class SpeedCommand {
        @NotNull
        @DecimalMin("0.0")
        private Double speed;

        /** @return desired speed */
        public Double getSpeed() {
            return speed;
        }

        /** @param speed desired speed */
        public void setSpeed(Double speed) {
            this.speed = speed;
        }
    }
}
