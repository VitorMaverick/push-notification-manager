package br.edu.acad.ifma.app.domain.device;

import br.edu.acad.ifma.app.domain.shared.FcmToken;
import java.time.Instant;

public class Device {

    private Long id;
    private FcmToken fcmToken;
    private String deviceName;
    private DeviceType type;
    private DeviceStatus status;
    private Instant registeredAt;
    private Instant lastUsedAt;

    private Device() {}

    public Long getId() {
        return id;
    }

    public FcmToken getFcmToken() {
        return fcmToken;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public DeviceType getType() {
        return type;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public static DeviceBuilder builder() {
        return new DeviceBuilder();
    }

    public static final class DeviceBuilder {

        private Long id;
        private FcmToken fcmToken;
        private String deviceName;
        private DeviceType type;
        private DeviceStatus status;
        private Instant registeredAt;
        private Instant lastUsedAt;

        private DeviceBuilder() {}

        public DeviceBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public DeviceBuilder withFcmToken(FcmToken fcmToken) {
            this.fcmToken = fcmToken;
            return this;
        }

        public DeviceBuilder withDeviceName(String deviceName) {
            this.deviceName = deviceName;
            return this;
        }

        public DeviceBuilder withType(DeviceType type) {
            this.type = type;
            return this;
        }

        public DeviceBuilder withStatus(DeviceStatus status) {
            this.status = status;
            return this;
        }

        public DeviceBuilder withRegisteredAt(Instant registeredAt) {
            this.registeredAt = registeredAt;
            return this;
        }

        public DeviceBuilder withLastUsedAt(Instant lastUsedAt) {
            this.lastUsedAt = lastUsedAt;
            return this;
        }

        public Device build() {
            Device device = new Device();
            device.id = this.id;
            device.fcmToken = this.fcmToken;
            device.deviceName = this.deviceName;
            device.type = this.type;
            device.status = this.status;
            device.registeredAt = this.registeredAt;
            device.lastUsedAt = this.lastUsedAt;
            return device;
        }
    }
}
