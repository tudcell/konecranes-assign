package com.example.konecranes.adapter.out.process;

import com.example.konecranes.application.port.out.VehicleProcessHandle;
import com.example.konecranes.application.port.out.VehicleProcessLauncherPort;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Process adapter that launches vehicle JVM processes.
 *
 * Uses ProcessBuilder to start child JVMs and wraps the resulting
 * Process in a VehicleProcessHandle so the application layer
 * does not depend directly on java.lang.Process.
 */
@Component
public class JvmVehicleProcessLauncher implements VehicleProcessLauncherPort {

    /**
     * Launches one vehicle process using the provided command.
     *
     * Standard error is merged into standard output and inherited
     * by the current process for easier runtime debugging.
     *
     * @param command full JVM command used to start the vehicle process
     * @return handle for the launched process
     * @throws IOException when process startup fails
     */
    @Override
    public VehicleProcessHandle launch(List<String> command) throws IOException {
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .inheritIO()
                .start();
        return new JvmVehicleProcessHandle(process);
    }

    /**
     * JVM-backed implementation of the generic vehicle process handle.
     *
     * Wraps java.lang.Process so the application layer works with
     * a port abstraction instead of a JVM-specific process type.
     */
    private static class JvmVehicleProcessHandle implements VehicleProcessHandle {

        private final Process process;

        private JvmVehicleProcessHandle(Process process) {
            this.process = process;
        }

        /**
         * Checks whether the child process is still running.
         *
         * @return true when the process is alive
         */
        @Override
        public boolean isAlive() {
            return process.isAlive();
        }

        /**
         * Requests graceful process termination.
         */
        @Override
        public void destroy() {
            process.destroy();
        }

        /**
         * Forces process termination immediately.
         */
        @Override
        public void destroyForcibly() {
            process.destroyForcibly();
        }

        /**
         * Waits for the process to exit within the given timeout.
         *
         * @param timeoutMillis maximum wait time in milliseconds
         * @return true if the process exited in time
         * @throws InterruptedException when the waiting thread is interrupted
         */
        @Override
        public boolean waitFor(long timeoutMillis) throws InterruptedException {
            return process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
        }
    }
}