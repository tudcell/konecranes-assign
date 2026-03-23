package com.example.konecranes.controller;

import com.example.konecranes.service.port.in.VehicleControlUseCase;
import com.example.konecranes.service.port.in.VehicleSpawnUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

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

    @PostMapping("/spawn")
    @ResponseStatus(HttpStatus.CREATED)
    public SpawnResponse spawn(@Valid @RequestBody SpawnRequest request) throws IOException {
        List<String> vehicleIds = vehicleSpawnerService.spawn(request.getCount());
        return new SpawnResponse(vehicleIds);
    }

    @PostMapping("/{vehicleId}/direction")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void overrideDirection(@PathVariable String vehicleId,
                                  @Valid @RequestBody DirectionCommand request) throws IOException {
        vehicleCommandService.overrideDirection(vehicleId, request.getDirectionDeg());
    }

    @PostMapping("/{vehicleId}/speed")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void overrideSpeed(@PathVariable String vehicleId,
                              @Valid @RequestBody SpeedCommand request) throws IOException {
        vehicleCommandService.overrideSpeed(vehicleId, request.getSpeed());
    }

    public static class SpawnRequest {
        @Min(1)
        @Max(25)
        private int count;

        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    public static class SpawnResponse {
        private final List<String> vehicleIds;

        public SpawnResponse(List<String> vehicleIds) {
            this.vehicleIds = vehicleIds;
        }

        public List<String> getVehicleIds() {
            return vehicleIds;
        }
    }

    public static class DirectionCommand {
        @NotNull
        private Double directionDeg;

        public Double getDirectionDeg() { return directionDeg; }
        public void setDirectionDeg(Double directionDeg) { this.directionDeg = directionDeg; }
    }

    public static class SpeedCommand {
        @NotNull
        @DecimalMin("0.0")
        private Double speed;

        public Double getSpeed() { return speed; }
        public void setSpeed(Double speed) { this.speed = speed; }
    }
}
