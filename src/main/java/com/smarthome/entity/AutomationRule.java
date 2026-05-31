package com.smarthome.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "automation_rules")
public class AutomationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sensorId;

    @Column(nullable = false)
    private Long deviceId;

    @Column(nullable = false)
    private boolean enabled;

    public AutomationRule() {
    }

    public AutomationRule(Long sensorId, Long deviceId, boolean enabled) {
        this.sensorId = sensorId;
        this.deviceId = deviceId;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSensorId() {
        return sensorId;
    }

    public void setSensorId(Long sensorId) {
        this.sensorId = sensorId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
