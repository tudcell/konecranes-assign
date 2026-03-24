package com.example.konecranes.application.port.out;

import java.io.IOException;
import java.util.List;

public interface VehicleProcessLauncherPort {
    VehicleProcessHandle launch(List<String> command) throws IOException;
}


