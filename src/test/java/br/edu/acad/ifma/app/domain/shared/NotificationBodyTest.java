package br.edu.acad.ifma.app.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.edu.acad.ifma.app.domain.shared.exception.DomainException;
import org.junit.jupiter.api.Test;

class NotificationBodyTest {

    @Test
    void valid_body_created() {
        NotificationBody body = new NotificationBody("Hello World");
        assertThat(body.value()).isEqualTo("Hello World");
    }

    @Test
    void blank_body_throws() {
        assertThatThrownBy(() -> new NotificationBody("   "))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("blank");
    }

    @Test
    void null_body_throws() {
        assertThatThrownBy(() -> new NotificationBody(null))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("blank");
    }

    @Test
    void too_long_body_throws() {
        String longBody = "a".repeat(2001);
        assertThatThrownBy(() -> new NotificationBody(longBody))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("2000");
    }
}
