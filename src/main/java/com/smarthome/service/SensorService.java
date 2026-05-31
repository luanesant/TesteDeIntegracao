package com.smarthome.service;

import com.smarthome.entity.Sensor;
import com.smarthome.exception.SensorInactiveException;
import com.smarthome.exception.SensorNotFoundException;
import com.smarthome.repository.SensorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SensorService {

    private final SensorRepository sensorRepository;
    private final AutomationService automationService;

    public SensorService(SensorRepository sensorRepository, AutomationService automationService) {
        this.sensorRepository = sensorRepository;
        this.automationService = automationService;
    }

    @Transactional
    public void detectMotion(Long sensorId) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new SensorNotFoundException("Sensor not found with id: " + sensorId));

        if (!sensor.isActive()) {
            throw new SensorInactiveException("Sensor '" + sensor.getName() + "' is inactive and cannot detect motion");
        }

        automationService.executeRulesForSensor(sensorId);
    }
}
