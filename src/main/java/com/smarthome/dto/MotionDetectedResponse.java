package com.smarthome.dto;

import java.util.List;

public class MotionDetectedResponse {

    private Long sensorId;
    private String sensorName;
    private int devicesActivated;
    private List<String> activatedDeviceNames;

    public MotionDetectedResponse() {
    }

    public MotionDetectedResponse(Long sensorId, String sensorName, int devicesActivated, List<String> activatedDeviceNames) {
        this.sensorId = sensorId;
        this.sensorName = sensorName;
        this.devicesActivated = devicesActivated;
        this.activatedDeviceNames = activatedDeviceNames;
    }

    public Long getSensorId() {
        return sensorId;
    }

    public void setSensorId(Long sensorId) {
        this.sensorId = sensorId;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public int getDevicesActivated() {
        return devicesActivated;
    }

    public void setDevicesActivated(int devicesActivated) {
        this.devicesActivated = devicesActivated;
    }

    public List<String> getActivatedDeviceNames() {
        return activatedDeviceNames;
    }

    public void setActivatedDeviceNames(List<String> activatedDeviceNames) {
        this.activatedDeviceNames = activatedDeviceNames;
    }
}
