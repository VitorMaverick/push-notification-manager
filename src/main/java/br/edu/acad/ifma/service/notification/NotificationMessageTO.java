package br.edu.acad.ifma.service.notification;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Builder pattern for a message to be sent (MensagemEnviada).
 * Fields are optional and can be composed fluently.
 */
public final class NotificationMessageTO {

    private final String titulo;
    private final String corpo;
    private final String imagemUrl;
    private final Map<String, String> dados;

    private NotificationMessageTO(Builder b) {
        this.titulo = b.titulo;
        this.corpo = b.corpo;
        this.imagemUrl = b.imagemUrl;
        this.dados = Collections.unmodifiableMap(new HashMap<>(b.dados));
    }

    public String getTitulo() {
        return titulo;
    }

    public String getCorpo() {
        return corpo;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public Map<String, String> getDados() {
        return dados;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String titulo;
        private String corpo;
        private String imagemUrl;
        private final Map<String, String> dados = new HashMap<>();

        private Builder() {}

        public Builder titulo(String titulo) {
            this.titulo = titulo;
            return this;
        }

        public Builder corpo(String corpo) {
            this.corpo = corpo;
            return this;
        }

        public Builder imagemUrl(String imagemUrl) {
            this.imagemUrl = imagemUrl;
            return this;
        }

        public Builder dado(String chave, String valor) {
            Objects.requireNonNull(chave, "chave não pode ser nula");
            if (valor != null) {
                this.dados.put(chave, valor);
            }
            return this;
        }

        public Builder dados(Map<String, String> mapa) {
            if (mapa != null) {
                mapa.forEach((k, v) -> {
                    if (k != null && v != null) {
                        this.dados.put(k, v);
                    }
                });
            }
            return this;
        }

        public NotificationMessageTO build() {
            return new NotificationMessageTO(this);
        }
    }
}
