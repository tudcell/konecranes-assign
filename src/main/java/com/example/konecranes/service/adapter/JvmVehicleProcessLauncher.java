package com.example.konecranes.service.adapter;

import com.example.konecranes.service.port.out.VehicleProcessHandle;
import com.example.konecranes.service.port.out.VehicleProcessLauncherPort;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.List;

@Component
public class JvmVehicleProcessLauncher implements VehicleProcessLauncherPort {

    @Override
    public VehicleProcessHandle launch(List<String> command) throws IOException {
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .inheritIO()
                .start();
        return new JvmVehicleProcessHandle(process);
    }

    private static class JvmVehicleProcessHandle implements VehicleProcessHandle {
        private final Process process;

        private JvmVehicleProcessHandle(Process process) {
            this.process = process;
        }

        @Override
        public boolean isAlive() {
            return process.isAlive();
        }

        @Override
        public void destroy() {
            process.destroy();
        }

        @Override
        public void destroyForcibly() {
            process.destroyForcibly();
        }

        @Override
        public boolean waitFor(long timeoutMillis) throws InterruptedException {
            return process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
        }
    }
}

