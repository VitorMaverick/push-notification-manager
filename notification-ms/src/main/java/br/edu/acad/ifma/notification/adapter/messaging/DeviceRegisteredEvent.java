package br.edu.acad.ifma.notification.adapter.messaging;

import java.io.Serializable;
import java.time.Instant;

/**
 * Consumed from device.registered.queue.
 * Duplicate of device-service's event — controlled duplication avoids shared-lib coupling.
 */
public class DeviceRegisteredEvent implements Serializable {

    private Long deviceId;
    private String fcmToken;
    private String platform;
    private Instant registeredAt;

    public DeviceRegisteredEvent() {}

    public Long getDeviceId() { return deviceId; }
    public String getFcmToken() { return fcmToken; }
    public String getPlatform() { return platform; }
    public Instant getRegisteredAt() { return registeredAt; }

    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public void setPlatform(String platform) { this.platform = platform; }
    public void setRegisteredAt(Instant registeredAt) { this.registeredAt = registeredAt; }
}
