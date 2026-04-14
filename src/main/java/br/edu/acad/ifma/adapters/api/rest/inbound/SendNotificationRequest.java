package br.edu.acad.ifma.adapters.api.rest.inbound;

import jakarta.validation.constraints.NotBlank;

public class SendNotificationRequest {

    @NotBlank
    private String deviceToken;

    @NotBlank
    private String title;

    @NotBlank
    private String body;

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
