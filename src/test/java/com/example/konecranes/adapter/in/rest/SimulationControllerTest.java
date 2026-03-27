package com.example.konecranes.adapter.in.rest;

import com.example.konecranes.application.port.in.SimulationQueryUseCase;
import com.example.konecranes.application.port.in.SimulationStreamUseCase;
import com.example.konecranes.config.SseProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SimulationController.class)
class SimulationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SimulationQueryUseCase simulationQueryUseCase;
    @MockBean
    private SimulationStreamUseCase simulationStreamUseCase;
    @MockBean
    private SseProperties sseProperties;

    @BeforeEach
    void setup() {
        when(sseProperties.getEmitterTimeoutMillis()).thenReturn(1000L);
    }

    @Test
    void getSimulationSnapshot_returnsOk() throws Exception {
        mockMvc.perform(get("/api/simulation/snapshot"))
                .andExpect(status().isOk());
    }

    @Test
    void streamSimulationSnapshot_returnsOk() throws Exception {
        mockMvc.perform(get("/api/simulation/stream"))
                .andExpect(status().isOk());
    }
}
