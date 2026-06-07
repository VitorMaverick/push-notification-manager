package br.edu.acad.ifma.device.port;

import br.edu.acad.ifma.device.domain.Device;
import br.edu.acad.ifma.device.domain.FcmToken;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeviceRepositoryPort {
    Device save(Device device);
    Optional<Device> findByFcmToken(FcmToken token);
    Page<Device> findAll(Pageable pageable);
    boolean existsByFcmToken(FcmToken token);
}
