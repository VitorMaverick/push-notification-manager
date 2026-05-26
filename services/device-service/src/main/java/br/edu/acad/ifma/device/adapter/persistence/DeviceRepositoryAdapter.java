package br.edu.acad.ifma.device.adapter.persistence;

import br.edu.acad.ifma.device.domain.Device;
import br.edu.acad.ifma.device.domain.DeviceStatus;
import br.edu.acad.ifma.device.domain.DeviceType;
import br.edu.acad.ifma.device.domain.FcmToken;
import br.edu.acad.ifma.device.port.DeviceRepositoryPort;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class DeviceRepositoryAdapter implements DeviceRepositoryPort {

    private final DeviceJpaRepository jpaRepository;

    public DeviceRepositoryAdapter(DeviceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Device save(Device device) {
        return toDomain(jpaRepository.save(toEntity(device)));
    }

    @Override
    public Optional<Device> findByFcmToken(FcmToken token) {
        return jpaRepository.findByFcmToken(token.value()).map(this::toDomain);
    }

    @Override
    public Page<Device> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(this::toDomain);
    }

    @Override
    public boolean existsByFcmToken(FcmToken token) {
        return jpaRepository.existsByFcmToken(token.value());
    }

    private DeviceJpaEntity toEntity(Device d) {
        DeviceJpaEntity e = new DeviceJpaEntity();
        e.setId(d.getId());
        e.setFcmToken(d.getFcmToken().value());
        e.setDeviceName(d.getDeviceName());
        e.setType(d.getType());
        e.setStatus(d.getStatus() != null ? d.getStatus() : DeviceStatus.ACTIVE);
        e.setRegisteredAt(d.getRegisteredAt());
        e.setLastUsedAt(d.getLastUsedAt());
        return e;
    }

    private Device toDomain(DeviceJpaEntity e) {
        return Device.builder()
            .withId(e.getId())
            .withFcmToken(new FcmToken(e.getFcmToken()))
            .withDeviceName(e.getDeviceName())
            .withType(e.getType() != null ? DeviceType.valueOf(e.getType().name()) : null)
            .withStatus(e.getStatus())
            .withRegisteredAt(e.getRegisteredAt())
            .withLastUsedAt(e.getLastUsedAt())
            .build();
    }
}
