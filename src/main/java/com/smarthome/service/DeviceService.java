package com.smarthome.service;

import com.smarthome.entity.Device;
import com.smarthome.entity.DeviceStatus;
import com.smarthome.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Transactional
    public Device turnOn(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + deviceId));
        device.setStatus(DeviceStatus.ON);
        return deviceRepository.save(device);
    }

    @Transactional
    public Device turnOff(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + deviceId));
        device.setStatus(DeviceStatus.OFF);
        return deviceRepository.save(device);
    }

    public Device findById(Long deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + deviceId));
    }
}
