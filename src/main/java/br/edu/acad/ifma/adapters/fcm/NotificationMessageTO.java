package br.edu.acad.ifma.adapters.fcm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Builder pattern for a message to be sent via FCM (adapter-level Transfer Object).
 * Field names use English (title/body) to keep consistency across the project.
 */
public final class NotificationMessageTO {

    private final String title;
    private final String body;
    private final String imageUrl;
    private final Map<String, String> data;

    private NotificationMessageTO(Builder b) {
        this.title = b.title;
        this.body = b.body;
        this.imageUrl = b.imageUrl;
        this.data = Collections.unmodifiableMap(new HashMap<>(b.data));
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Map<String, String> getData() {
        return data;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String title;
        private String body;
        private String imageUrl;
        private final Map<String, String> data = new HashMap<>();

        private Builder() {}

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder data(String key, String value) {
            Objects.requireNonNull(key, "key cannot be null");
            if (value != null) {
                this.data.put(key, value);
            }
            return this;
        }

        public Builder dataMap(Map<String, String> map) {
            if (map != null) {
                map.forEach((k, v) -> {
                    if (k != null && v != null) {
                        this.data.put(k, v);
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
