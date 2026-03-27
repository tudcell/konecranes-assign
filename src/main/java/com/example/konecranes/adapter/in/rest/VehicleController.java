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
    @Setter
    @Getter
    public static class SpawnRequest {
        /**
         *
         * -- SETTER --
         *
         @return number of vehicles to spawn
          * @param count number of vehicles to spawn
         */
        @Min(1)
        @Max(25)
        private int count;

    }

    /**
     * Output payload containing created vehicle ids.
     */
    @Getter
    public static class SpawnResponse {
        /**
         * @return spawned vehicle identifiers
         */
        private final List<String> vehicleIds;

        /**
         * @param vehicleIds spawned vehicle identifiers
         */
        public SpawnResponse(List<String> vehicleIds) {
            this.vehicleIds = vehicleIds;
        }

    }

    /**
     * Input payload for direction override.
     */
    @Setter
    @Getter
    public static class DirectionCommand {
        /**
         *
         * -- SETTER --
         *
         @return desired direction in degrees
          * @param directionDeg desired direction in degrees
         */
        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("359.99")
        private Double directionDeg;

    }

    /**
     * Input payload for speed override.
     */
    @Setter
    @Getter
    public static class SpeedCommand {
        /**
         *
         * -- SETTER --
         *
         @return desired speed
          * @param speed desired speed
         */
        @NotNull
        @DecimalMin("0.0")
        private Double speed;

    }
}
