package com.example.konecranes.service;

import com.example.konecranes.config.SimulationProperties;
import com.example.konecranes.service.port.out.VehicleProcessHandle;
import com.example.konecranes.service.port.out.VehicleProcessLauncherPort;
import com.example.konecranes.service.port.out.VehicleStateStore;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VehicleSpawnerServiceTest {

    @Test
    void spawnPassesReconnectArgumentsToVehicleProcess() throws IOException {
        Path fakeJar = Files.createTempFile("vehicle-test", ".jar");

        SimulationProperties properties = new SimulationProperties();
        properties.getGateway().setHost("127.0.0.1");
        properties.getGateway().setPort(9090);
        properties.getWorld().setWidth(1000.0);
        properties.getWorld().setHeight(700.0);
        properties.getVehicle().setJarPath(fakeJar.toString());
        properties.getVehicle().setDefaultSpeed(60.0);
        properties.getVehicle().setTickMillis(100L);
        properties.getVehicle().setReconnectMaxAttempts(7);
        properties.getVehicle().setReconnectInitialBackoffMillis(600L);
        properties.getVehicle().setReconnectMaxBackoffMillis(4200L);

        VehicleStateStore stateStore = mock(VehicleStateStore.class);
        when(stateStore.findAll()).thenReturn(Collections.emptyList());

        VehicleProcessHandle processHandle = mock(VehicleProcessHandle.class);
        VehicleProcessLauncherPort launcher = mock(VehicleProcessLauncherPort.class);
        when(launcher.launch(org.mockito.ArgumentMatchers.anyList())).thenReturn(processHandle);

        VehicleSpawnerService service = new VehicleSpawnerService(properties, stateStore, launcher);
        List<String> ids = service.spawn(1);

        assertEquals(1, ids.size());

        verify(launcher).launch(org.mockito.ArgumentMatchers.argThat(command ->
                command.contains("--reconnectMaxAttempts=7")
                        && command.contains("--reconnectInitialBackoffMillis=600")
                        && command.contains("--reconnectMaxBackoffMillis=4200")));

        Files.deleteIfExists(fakeJar);
    }

    @Test
    void stopSpawnedVehiclesTerminatesAliveChildProcesses() throws IOException, InterruptedException {
        Path fakeJar = Files.createTempFile("vehicle-test", ".jar");

        SimulationProperties properties = new SimulationProperties();
        properties.getGateway().setHost("127.0.0.1");
        properties.getGateway().setPort(9090);
        properties.getWorld().setWidth(1000.0);
        properties.getWorld().setHeight(700.0);
        properties.getVehicle().setJarPath(fakeJar.toString());
        properties.getVehicle().setDefaultSpeed(60.0);
        properties.getVehicle().setTickMillis(100L);

        VehicleStateStore stateStore = mock(VehicleStateStore.class);
        when(stateStore.findAll()).thenReturn(Collections.emptyList());

        VehicleProcessHandle processHandle = mock(VehicleProcessHandle.class);
        when(processHandle.isAlive()).thenReturn(true);
        when(processHandle.waitFor(3000L)).thenReturn(true);

        VehicleProcessLauncherPort launcher = mock(VehicleProcessLauncherPort.class);
        when(launcher.launch(org.mockito.ArgumentMatchers.anyList())).thenReturn(processHandle);

        VehicleSpawnerService service = new VehicleSpawnerService(properties, stateStore, launcher);
        service.spawn(1);
        service.stopSpawnedVehicles();

        verify(processHandle).destroy();
        verify(processHandle, atLeastOnce()).waitFor(3000L);
        verify(processHandle).isAlive();
        verify(processHandle, org.mockito.Mockito.never()).destroyForcibly();

        Files.deleteIfExists(fakeJar);
    }
}



