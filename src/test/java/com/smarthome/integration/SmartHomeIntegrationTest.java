package com.smarthome.integration;

import com.smarthome.entity.*;
import com.smarthome.exception.SensorInactiveException;
import com.smarthome.repository.*;
import com.smarthome.service.SensorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração principais do sistema Smart Home.
 * Valida a comunicação entre Services, Repositories e banco de dados.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SmartHomeIntegrationTest {

    @Autowired
    private SensorService sensorService;

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
        Sensor sensor = new Sensor("Motion Sensor - Living Room", true);
        sensor = sensorRepository.save(sensor);
        sensorId = sensor.getId();
    }

    private void createDevice(){
        Device lamp = new Device("Lamp", DeviceStatus.OFF);
        lamp = deviceRepository.save(lamp);
        lampId = lamp.getId();
    }

    private void cleanAutomationRule(){
        AutomationRule rule = new AutomationRule(sensorId, lampId, true);
        automationRuleRepository.save(rule);
    }

    @Test
    @DisplayName("Deve ligar a lâmpada quando movimento for detectado")
    void shouldTurnLightOnWhenMotionDetected() {
        sensorService.detectMotion(sensorId);

        Device lamp = deviceRepository.findById(lampId).get();
        assertEquals(DeviceStatus.ON, lamp.getStatus());

        assertTrue(logEntryRepository.existsByAction("Lamp automatically turned on"));
    }

    @Test
    @DisplayName("Lâmpada permanece OFF quando não existir regra de automação ativa")
    void shouldNotTurnLightOnWhenNoActiveRuleExists() {
        List<AutomationRule> rules = automationRuleRepository.findAll();
        rules.forEach(rule -> {
            rule.setEnabled(false);
            automationRuleRepository.save(rule);
        });

        sensorService.detectMotion(sensorId);

        Device lamp = deviceRepository.findById(lampId).get();
        assertEquals(DeviceStatus.OFF, lamp.getStatus());

        assertFalse(logEntryRepository.existsByAction("Lamp automatically turned on"));
        assertEquals(0, logEntryRepository.count());
    }

    @Test
    @DisplayName("Exceção lançada quando sensor estiver desativado")
    void shouldThrowExceptionWhenSensorIsInactive() {
        Sensor sensor = sensorRepository.findById(sensorId).get();
        sensor.setActive(false);
        sensorRepository.save(sensor);

        SensorInactiveException exception = assertThrows(
                SensorInactiveException.class,
                () -> sensorService.detectMotion(sensorId)
        );

        assertTrue(exception.getMessage().contains("inactive"));

        Device lamp = deviceRepository.findById(lampId).get();
        assertEquals(DeviceStatus.OFF, lamp.getStatus());

        assertEquals(0, logEntryRepository.count());
    }

    @Test
    @DisplayName("Todas as lâmpadas ligadas quando múltiplas estiverem associadas ao sensor")
    void shouldTurnOnAllLampsWhenMultipleDevicesAssociatedToSensor() {
        Device lamp2 = new Device("Lamp Kitchen", DeviceStatus.OFF);
        lamp2 = deviceRepository.save(lamp2);

        Device lamp3 = new Device("Lamp Bedroom", DeviceStatus.OFF);
        lamp3 = deviceRepository.save(lamp3);

        automationRuleRepository.save(new AutomationRule(sensorId, lamp2.getId(), true));
        automationRuleRepository.save(new AutomationRule(sensorId, lamp3.getId(), true));

        sensorService.detectMotion(sensorId);

        assertEquals(DeviceStatus.ON, deviceRepository.findById(lampId).get().getStatus());
        assertEquals(DeviceStatus.ON, deviceRepository.findById(lamp2.getId()).get().getStatus());
        assertEquals(DeviceStatus.ON, deviceRepository.findById(lamp3.getId()).get().getStatus());

        assertTrue(logEntryRepository.existsByAction("Lamp automatically turned on"));
        assertTrue(logEntryRepository.existsByAction("Lamp Kitchen automatically turned on"));
        assertTrue(logEntryRepository.existsByAction("Lamp Bedroom automatically turned on"));

        assertEquals(3, logEntryRepository.count());
    }
}
