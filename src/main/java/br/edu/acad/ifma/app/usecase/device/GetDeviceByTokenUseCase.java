package br.edu.acad.ifma.app.usecase.device;

import br.edu.acad.ifma.app.domain.device.Device;
import br.edu.acad.ifma.app.domain.shared.FcmToken;
import br.edu.acad.ifma.app.domain.shared.exception.DeviceNotFoundException;
import br.edu.acad.ifma.app.port.DeviceRepositoryPort;
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
        FcmToken fcmToken = new FcmToken(token);
        return deviceRepository
            .findByFcmToken(fcmToken)
            .orElseThrow(() -> new DeviceNotFoundException("Device not found for token: " + token));
    }
}
