package com.example.konecranes.adapter.in.rest;

import com.example.konecranes.application.port.in.VehicleControlUseCase;
import com.example.konecranes.application.port.in.VehicleSpawnUseCase;
import com.example.konecranes.application.port.in.UpdateVehicleStateUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleController.class)
class VehicleControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private VehicleControlUseCase vehicleControlUseCase;
    @MockBean
    private VehicleSpawnUseCase vehicleSpawnUseCase;
    @MockBean
    private UpdateVehicleStateUseCase updateVehicleStateUseCase;

    @Test
    void overrideDirection_returnsAccepted() throws Exception {
        mockMvc.perform(post("/api/vehicles/VH-1/direction")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"directionDeg\":90.0}"))
                .andExpect(status().isAccepted());
    }

    @Test
    void overrideSpeed_returnsAccepted() throws Exception {
        mockMvc.perform(post("/api/vehicles/VH-1/speed")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"speed\":10.0}"))
                .andExpect(status().isAccepted());
    }
}
