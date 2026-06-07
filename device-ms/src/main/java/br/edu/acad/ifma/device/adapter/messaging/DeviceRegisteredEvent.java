package br.edu.acad.ifma.device.adapter.messaging;

import java.io.Serializable;
import java.time.Instant;

public class DeviceRegisteredEvent implements Serializable {

    private Long deviceId;
    private String fcmToken;
    private String platform;
    private Instant registeredAt;

    public DeviceRegisteredEvent() {}

    public DeviceRegisteredEvent(Long deviceId, String fcmToken, String platform, Instant registeredAt) {
        this.deviceId = deviceId;
        this.fcmToken = fcmToken;
        this.platform = platform;
        this.registeredAt = registeredAt;
    }

    public Long getDeviceId() { return deviceId; }
    public String getFcmToken() { return fcmToken; }
    public String getPlatform() { return platform; }
    public Instant getRegisteredAt() { return registeredAt; }

    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public void setPlatform(String platform) { this.platform = platform; }
    public void setRegisteredAt(Instant registeredAt) { this.registeredAt = registeredAt; }
}
