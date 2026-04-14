package br.edu.acad.ifma.app.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.edu.acad.ifma.app.domain.shared.exception.InvalidFcmTokenException;
import org.junit.jupiter.api.Test;

class FcmTokenTest {

    @Test
    void valid_token_created() {
        FcmToken token = new FcmToken("abcdefghijklmnopqrst"); // 20 chars
        assertThat(token.value()).isEqualTo("abcdefghijklmnopqrst");
    }

    @Test
    void blank_token_throws() {
        assertThatThrownBy(() -> new FcmToken(""))
            .isInstanceOf(InvalidFcmTokenException.class)
            .hasMessageContaining("blank");
    }

    @Test
    void null_token_throws() {
        assertThatThrownBy(() -> new FcmToken(null))
            .isInstanceOf(InvalidFcmTokenException.class)
            .hasMessageContaining("blank");
    }

    @Test
    void short_token_throws() {
        assertThatThrownBy(() -> new FcmToken("tooshort"))
            .isInstanceOf(InvalidFcmTokenException.class)
            .hasMessageContaining("too short");
    }
}
