package com.example.konecranes.service.adapter;

import com.example.konecranes.service.port.out.VehicleProcessLauncherPort;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class JvmVehicleProcessLauncher implements VehicleProcessLauncherPort {

    @Override
    public void launch(List<String> command) throws IOException {
        new ProcessBuilder(command)
                .redirectErrorStream(true)
                .inheritIO()
                .start();
    }
}

