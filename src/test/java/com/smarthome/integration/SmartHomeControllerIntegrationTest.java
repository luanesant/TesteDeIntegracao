package com.smarthome.integration;

import com.smarthome.entity.*;
import com.smarthome.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração do Controller com todas as camadas.
 * Valida requisições HTTP reais passando por Controller → Service → Repository → H2.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SmartHomeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private AutomationRuleRepository automationRuleRepository;

    @Autowired
    private LogEntryRepository logEntryRepository;

    private Long sensorId;
    private Long lampId;

    @BeforeEach
    void setUp() {
        cleanData();

        createSensor();
        createDevice(); 
        
        cleanAutomationRule();
    }


    private void cleanData(){
        logEntryRepository.deleteAll();
        automationRuleRepository.deleteAll();
        deviceRepository.deleteAll();
        sensorRepository.deleteAll();
    }

    private void createSensor(){
        Sensor sensor = new Sensor("Motion Sensor - Hall", true);
        sensor = sensorRepository.save(sensor);
        sensorId = sensor.getId();
    }

    private void createDevice(){
        Device lamp = new Device("Hall Lamp", DeviceStatus.OFF);
        lamp = deviceRepository.save(lamp);
        lampId = lamp.getId();
    }

    private void cleanAutomationRule(){
        AutomationRule rule = new AutomationRule(sensorId, lampId, true);
        automationRuleRepository.save(rule);
    }


    @Test
    @DisplayName("POST /api/smarthome/sensors/{id}/motion - deve acionar lâmpada via HTTP")
    void shouldTurnOnLampViaHttpRequest() throws Exception {
        mockMvc.perform(post("/api/smarthome/sensors/" + sensorId + "/motion")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sensorId").value(sensorId))
                .andExpect(jsonPath("$.sensorName").value("Motion Sensor - Hall"))
                .andExpect(jsonPath("$.devicesActivated").value(1))
                .andExpect(jsonPath("$.activatedDeviceNames[0]").value("Hall Lamp"));

        Device lamp = deviceRepository.findById(lampId).get();
        assertEquals(DeviceStatus.ON, lamp.getStatus());
        assertTrue(logEntryRepository.existsByAction("Hall Lamp automatically turned on"));
    }

    @Test
    @DisplayName("POST /api/smarthome/sensors/{id}/motion - deve retornar 400 para sensor inativo")
    void shouldReturn400WhenSensorIsInactive() throws Exception {
        Sensor sensor = sensorRepository.findById(sensorId).get();
        sensor.setActive(false);
        sensorRepository.save(sensor);

        mockMvc.perform(post("/api/smarthome/sensors/" + sensorId + "/motion")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        Device lamp = deviceRepository.findById(lampId).get();
        assertEquals(DeviceStatus.OFF, lamp.getStatus());
        assertEquals(0, logEntryRepository.count());
    }

    @Test
    @DisplayName("POST /api/smarthome/sensors/{id}/motion - deve retornar 404 para sensor inexistente")
    void shouldReturn404WhenSensorNotFound() throws Exception {
        mockMvc.perform(post("/api/smarthome/sensors/9999999/motion")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("GET /api/smarthome/sensors - deve listar todos os sensores")
    void shouldListAllSensors() throws Exception {
        mockMvc.perform(get("/api/smarthome/sensors")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Motion Sensor - Hall"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    @DisplayName("GET /api/smarthome/devices - deve listar todos os dispositivos")
    void shouldListAllDevices() throws Exception {
        mockMvc.perform(get("/api/smarthome/devices")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Hall Lamp"))
                .andExpect(jsonPath("$[0].status").value("OFF"));
    }
}
