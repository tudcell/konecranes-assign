package com.example.konecranes.application.port.in;

import java.io.IOException;
import java.util.List;

public interface VehicleSpawnUseCase {
    List<String> spawn(int count) throws IOException;
}


