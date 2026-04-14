package br.edu.acad.ifma.adapters.repository;

import br.edu.acad.ifma.adapters.factory.DeviceFactory;
import br.edu.acad.ifma.adapters.model.DeviceJpaEntity;
import br.edu.acad.ifma.app.domain.device.Device;
import br.edu.acad.ifma.app.domain.shared.FcmToken;
import br.edu.acad.ifma.app.port.DeviceRepositoryPort;
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
        DeviceJpaEntity entity = DeviceFactory.toEntity(device);
        return DeviceFactory.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Device> findByFcmToken(FcmToken token) {
        return jpaRepository.findByFcmToken(token.value()).map(DeviceFactory::toDomain);
    }

    @Override
    public Page<Device> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(DeviceFactory::toDomain);
    }

    @Override
    public boolean existsByFcmToken(FcmToken token) {
        return jpaRepository.existsByFcmToken(token.value());
    }
}
