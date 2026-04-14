package br.edu.acad.ifma.app.usecase.device;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.edu.acad.ifma.app.domain.device.Device;
import br.edu.acad.ifma.app.domain.device.DeviceStatus;
import br.edu.acad.ifma.app.domain.shared.exception.DuplicateDeviceTokenException;
import br.edu.acad.ifma.app.port.DeviceRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegisterDeviceUseCaseTest {

    private static final String VALID_TOKEN = "abcdefghijklmnopqrstuvwxyz";

    private DeviceRepositoryPort deviceRepository;
    private RegisterDeviceUseCase useCase;

    @BeforeEach
    void setUp() {
        deviceRepository = mock(DeviceRepositoryPort.class);
        useCase = new RegisterDeviceUseCase(deviceRepository);
        when(deviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void duplicate_token_throws() {
        when(deviceRepository.existsByFcmToken(any())).thenReturn(true);
        RegisterDeviceCommand cmd = new RegisterDeviceCommand(VALID_TOKEN, "ANDROID", "Samsung");

        assertThatThrownBy(() -> useCase.execute(cmd)).isInstanceOf(DuplicateDeviceTokenException.class);
    }

    @Test
    void valid_command_saves_device() {
        when(deviceRepository.existsByFcmToken(any())).thenReturn(false);
        RegisterDeviceCommand cmd = new RegisterDeviceCommand(VALID_TOKEN, "IOS", "iPhone 14");

        Device result = useCase.execute(cmd);

        assertThat(result.getStatus()).isEqualTo(DeviceStatus.ACTIVE);
        assertThat(result.getFcmToken().value()).isEqualTo(VALID_TOKEN);
        assertThat(result.getDeviceName()).isEqualTo("iPhone 14");
        assertThat(result.getRegisteredAt()).isNotNull();
    }
}
