package com.example.konecranes.service.port.out;

import java.io.IOException;
import java.util.List;

public interface VehicleProcessLauncherPort {
    void launch(List<String> command) throws IOException;
}

