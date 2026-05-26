package br.edu.acad.ifma.device.usecase;

import br.edu.acad.ifma.device.domain.Device;
import br.edu.acad.ifma.device.domain.FcmToken;
import br.edu.acad.ifma.device.port.DeviceRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetDeviceByTokenUseCase {

    private final DeviceRepositoryPort deviceRepository;

    public GetDeviceByTokenUseCase(DeviceRepositoryPort deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public Device execute(String token) {
        return deviceRepository.findByFcmToken(new FcmToken(token))
            .orElseThrow(() -> new EntityNotFoundException("Device not found for token: " + token));
    }
}
