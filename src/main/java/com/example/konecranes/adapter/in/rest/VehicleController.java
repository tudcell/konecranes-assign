package com.example.konecranes.adapter.in.rest;

import com.example.konecranes.application.port.in.VehicleControlUseCase;
import com.example.konecranes.application.port.in.VehicleSpawnUseCase;
import lombok.Getter;
import lombok.Setter;
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
 * REST controller for vehicle spawning and manual control actions.
 *
 * Exposes endpoints for:
 * - spawning one or more vehicle processes
 * - overriding vehicle direction
 * - overriding vehicle speed
 */
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleSpawnUseCase vehicleSpawnUseCase;
    private final VehicleControlUseCase vehicleControlUseCase;

    public VehicleController(VehicleSpawnUseCase vehicleSpawnUseCase,
                             VehicleControlUseCase vehicleControlUseCase) {
        this.vehicleSpawnUseCase = vehicleSpawnUseCase;
        this.vehicleControlUseCase = vehicleControlUseCase;
    }

    /**
     * Spawns one or more new vehicle processes.
     *
     * @param request request payload containing the number of vehicles to create
     * @return response containing the ids of created vehicles
     * @throws IOException when process spawning fails
     */
    @PostMapping("/spawn")
    @ResponseStatus(HttpStatus.CREATED)
    public SpawnResponse spawn(@Valid @RequestBody SpawnRequest request) throws IOException {
        List<String> vehicleIds = vehicleSpawnUseCase.spawn(request.getCount());
        return new SpawnResponse(vehicleIds);
    }

    /**
     * Applies a manual direction override to one vehicle.
     *
     * @param vehicleId target vehicle id
     * @param request request payload containing desired direction
     * @throws IOException when command dispatch fails
     */
    @PostMapping("/{vehicleId}/direction")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void overrideDirection(@PathVariable String vehicleId,
                                  @Valid @RequestBody DirectionCommand request) throws IOException {
        vehicleControlUseCase.overrideDirection(vehicleId, request.getDirectionDeg());
    }

    /**
     * Applies a manual speed override to one vehicle.
     *
     * @param vehicleId target vehicle id
     * @param request request payload containing desired speed
     * @throws IOException when command dispatch fails
     */
    @PostMapping("/{vehicleId}/speed")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void overrideSpeed(@PathVariable String vehicleId,
                              @Valid @RequestBody SpeedCommand request) throws IOException {
        vehicleControlUseCase.overrideSpeed(vehicleId, request.getSpeed());
    }

    /**
     * Request payload for spawning vehicles.
     */
    @Getter
    @Setter
    public static class SpawnRequest {

        @Min(1)
        @Max(25)
        private int count;
    }

    /**
     * Response payload containing created vehicle ids.
     */
    @Getter
    public static class SpawnResponse {

        private final List<String> vehicleIds;

        public SpawnResponse(List<String> vehicleIds) {
            this.vehicleIds = vehicleIds;
        }
    }

    /**
     * Request payload for direction override.
     */
    @Getter
    @Setter
    public static class DirectionCommand {

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("359.99")
        private Double directionDeg;
    }

    /**
     * Request payload for speed override.
     */
    @Getter
    @Setter
    public static class SpeedCommand {

        @NotNull
        @DecimalMin("0.0")
        private Double speed;
    }
}