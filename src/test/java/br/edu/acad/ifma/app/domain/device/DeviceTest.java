package br.edu.acad.ifma.app.domain.device;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.acad.ifma.app.domain.shared.FcmToken;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class DeviceTest {

    private static final String VALID_TOKEN = "abcdefghijklmnopqrstuvwxyz";

    @Test
    void builder_creates_entity() {
        Instant now = Instant.now();
        Device device = Device.builder()
            .withId(1L)
            .withFcmToken(new FcmToken(VALID_TOKEN))
            .withDeviceName("Samsung Galaxy S21")
            .withType(DeviceType.ANDROID)
            .withStatus(DeviceStatus.ACTIVE)
            .withRegisteredAt(now)
            .build();

        assertThat(device.getId()).isEqualTo(1L);
        assertThat(device.getFcmToken().value()).isEqualTo(VALID_TOKEN);
        assertThat(device.getDeviceName()).isEqualTo("Samsung Galaxy S21");
        assertThat(device.getType()).isEqualTo(DeviceType.ANDROID);
        assertThat(device.getStatus()).isEqualTo(DeviceStatus.ACTIVE);
        assertThat(device.getRegisteredAt()).isEqualTo(now);
        assertThat(device.getLastUsedAt()).isNull();
    }
}
