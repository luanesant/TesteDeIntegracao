package com.smarthome.controller;

import com.smarthome.dto.MotionDetectedResponse;
import com.smarthome.entity.AutomationRule;
import com.smarthome.entity.Device;
import com.smarthome.entity.Sensor;
import com.smarthome.exception.SensorNotFoundException;
import com.smarthome.repository.AutomationRuleRepository;
import com.smarthome.repository.DeviceRepository;
import com.smarthome.repository.SensorRepository;
import com.smarthome.service.SensorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/smarthome")
public class SmartHomeController {

    private final SensorService sensorService;
    private final SensorRepository sensorRepository;
    private final DeviceRepository deviceRepository;
    private final AutomationRuleRepository automationRuleRepository;

    public SmartHomeController(SensorService sensorService,
                               SensorRepository sensorRepository,
                               DeviceRepository deviceRepository,
                               AutomationRuleRepository automationRuleRepository) {
        this.sensorService = sensorService;
        this.sensorRepository = sensorRepository;
        this.deviceRepository = deviceRepository;
        this.automationRuleRepository = automationRuleRepository;
    }

    @PostMapping("/sensors/{sensorId}/motion")
    public ResponseEntity<MotionDetectedResponse> detectMotion(@PathVariable Long sensorId) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() ->
                        new SensorNotFoundException(
                                "Sensor not found with id: " + sensorId));

        sensorService.detectMotion(sensorId);

        List<AutomationRule> rules = automationRuleRepository.findBySensorIdAndEnabledTrue(sensorId);
        List<String> deviceNames = rules.stream()
                .map(rule -> deviceRepository.findById(rule.getDeviceId()).orElse(null))
                .filter(device -> device != null)
                .map(Device::getName)
                .collect(Collectors.toList());

        MotionDetectedResponse response = new MotionDetectedResponse(
                sensor.getId(),
                sensor.getName(),
                deviceNames.size(),
                deviceNames
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sensors")
    public ResponseEntity<List<Sensor>> getAllSensors() {
        return ResponseEntity.ok(sensorRepository.findAll());
    }

    @GetMapping("/devices")
    public ResponseEntity<List<Device>> getAllDevices() {
        return ResponseEntity.ok(deviceRepository.findAll());
    }
}
