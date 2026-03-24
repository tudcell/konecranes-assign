package com.example.konecranes.application.port.in;

import java.io.IOException;

public interface RegisterVehicleSessionUseCase {
    void register(RegisterVehicleSessionCommand command) throws IOException;
}



