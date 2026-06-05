package br.edu.acad.ifma.device.usecase;

import br.edu.acad.ifma.device.domain.Device;
import br.edu.acad.ifma.device.domain.DeviceStatus;
import br.edu.acad.ifma.device.domain.DeviceType;
import br.edu.acad.ifma.device.domain.DuplicateDeviceTokenException;
import br.edu.acad.ifma.device.domain.FcmToken;
import br.edu.acad.ifma.device.port.DeviceEventPublisherPort;
import br.edu.acad.ifma.device.port.DeviceRepositoryPort;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterDeviceUseCase {

    private final DeviceRepositoryPort deviceRepository;
    private final DeviceEventPublisherPort eventPublisher;

    public RegisterDeviceUseCase(DeviceRepositoryPort deviceRepository, DeviceEventPublisherPort eventPublisher) {
        this.deviceRepository = deviceRepository;
        this.eventPublisher = eventPublisher;
    }

    public Device execute(RegisterDeviceCommand command) {
        FcmToken token = new FcmToken(command.fcmToken());
        if (deviceRepository.existsByFcmToken(token)) {
            throw new DuplicateDeviceTokenException("Token already registered: " + command.fcmToken());
        }
        Device device = Device.builder()
            .withFcmToken(token)
            .withDeviceName(command.userAgent())
            .withType(parseType(command.platform()))
            .withStatus(DeviceStatus.ACTIVE)
            .withRegisteredAt(Instant.now())
            .build();
        Device saved = deviceRepository.save(device);
        eventPublisher.publishDeviceRegistered(saved);
        return saved;
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
