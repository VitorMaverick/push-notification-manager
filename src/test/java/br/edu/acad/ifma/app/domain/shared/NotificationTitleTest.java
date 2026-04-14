package br.edu.acad.ifma.app.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.edu.acad.ifma.app.domain.shared.exception.DomainException;
import org.junit.jupiter.api.Test;

class NotificationTitleTest {

    @Test
    void valid_title_created() {
        NotificationTitle title = new NotificationTitle("Hello");
        assertThat(title.value()).isEqualTo("Hello");
    }

    @Test
    void blank_title_throws() {
        assertThatThrownBy(() -> new NotificationTitle(""))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("blank");
    }

    @Test
    void null_title_throws() {
        assertThatThrownBy(() -> new NotificationTitle(null))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("blank");
    }

    @Test
    void too_long_title_throws() {
        String longTitle = "a".repeat(256);
        assertThatThrownBy(() -> new NotificationTitle(longTitle))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("255");
    }
}
