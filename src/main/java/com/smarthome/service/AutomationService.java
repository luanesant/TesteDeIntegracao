package com.smarthome.service;

import com.smarthome.entity.AutomationRule;
import com.smarthome.entity.Device;
import com.smarthome.repository.AutomationRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AutomationService {

    private final AutomationRuleRepository automationRuleRepository;
    private final DeviceService deviceService;
    private final LogService logService;

    public AutomationService(AutomationRuleRepository automationRuleRepository,
                             DeviceService deviceService,
                             LogService logService) {
        this.automationRuleRepository = automationRuleRepository;
        this.deviceService = deviceService;
        this.logService = logService;
    }

    @Transactional
    public void executeRulesForSensor(Long sensorId) {
        List<AutomationRule> activeRules = automationRuleRepository.findBySensorIdAndEnabledTrue(sensorId);

        for (AutomationRule rule : activeRules) {
            Device device = deviceService.turnOn(rule.getDeviceId());
            logService.logAction(device.getName() + " automatically turned on");
        }
    }
}
