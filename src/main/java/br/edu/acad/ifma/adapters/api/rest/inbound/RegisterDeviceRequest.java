package br.edu.acad.ifma.adapters.api.rest.inbound;

import jakarta.validation.constraints.NotBlank;

public class RegisterDeviceRequest {

    @NotBlank
    private String fcmToken;

    private String platform;

    private String userAgent;

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
