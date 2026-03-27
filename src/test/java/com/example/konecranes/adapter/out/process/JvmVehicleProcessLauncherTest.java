package com.example.konecranes.adapter.out.process;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class JvmVehicleProcessLauncherTest {
    @Test
    void launchProcess_throwsIOExceptionOnFailure() {
        JvmVehicleProcessLauncher launcher = new JvmVehicleProcessLauncher();
        assertThrows(IOException.class, () -> launcher.launch(Arrays.asList("/not/a/real/path")));
    }
}
