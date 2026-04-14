package br.edu.acad.ifma.app.usecase.device;

import br.edu.acad.ifma.app.domain.device.Device;
import br.edu.acad.ifma.app.domain.device.DeviceStatus;
import br.edu.acad.ifma.app.domain.device.DeviceType;
import br.edu.acad.ifma.app.domain.shared.FcmToken;
import br.edu.acad.ifma.app.domain.shared.exception.DuplicateDeviceTokenException;
import br.edu.acad.ifma.app.port.DeviceRepositoryPort;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterDeviceUseCase {

    private final DeviceRepositoryPort deviceRepository;

    public RegisterDeviceUseCase(DeviceRepositoryPort deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public Device execute(RegisterDeviceCommand command) {
        FcmToken token = new FcmToken(command.getFcmToken());
        if (deviceRepository.existsByFcmToken(token)) {
            throw new DuplicateDeviceTokenException("Token already registered");
        }
        Device device = Device.builder()
            .withFcmToken(token)
            .withDeviceName(command.getUserAgent())
            .withType(parseType(command.getPlatform()))
            .withStatus(DeviceStatus.ACTIVE)
            .withRegisteredAt(Instant.now())
            .build();
        return deviceRepository.save(device);
    }

    private DeviceType parseType(String platform) {
        if (platform == null) return null;
        try {
            return DeviceType.valueOf(platform.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
