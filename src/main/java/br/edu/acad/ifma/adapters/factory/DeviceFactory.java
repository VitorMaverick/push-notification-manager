package br.edu.acad.ifma.adapters.factory;

import br.edu.acad.ifma.adapters.model.DeviceJpaEntity;
import br.edu.acad.ifma.app.domain.device.Device;
import br.edu.acad.ifma.app.domain.shared.FcmToken;

public abstract class DeviceFactory {

    private DeviceFactory() {}

    public static DeviceJpaEntity toEntity(Device domain) {
        DeviceJpaEntity e = new DeviceJpaEntity();
        e.setId(domain.getId());
        e.setFcmToken(domain.getFcmToken().value());
        e.setDeviceName(domain.getDeviceName());
        e.setType(domain.getType());
        e.setStatus(domain.getStatus());
        e.setRegisteredAt(domain.getRegisteredAt());
        e.setLastUsedAt(domain.getLastUsedAt());
        return e;
    }

    public static Device toDomain(DeviceJpaEntity e) {
        return Device.builder()
            .withId(e.getId())
            .withFcmToken(new FcmToken(e.getFcmToken()))
            .withDeviceName(e.getDeviceName())
            .withType(e.getType())
            .withStatus(e.getStatus())
            .withRegisteredAt(e.getRegisteredAt())
            .withLastUsedAt(e.getLastUsedAt())
            .build();
    }
}
