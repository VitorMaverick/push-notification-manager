package br.edu.acad.ifma.app.usecase.device;

public class RegisterDeviceCommand {

    private final String fcmToken;
    private final String platform;
    private final String userAgent;

    public RegisterDeviceCommand(String fcmToken, String platform, String userAgent) {
        this.fcmToken = fcmToken;
        this.platform = platform;
        this.userAgent = userAgent;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public String getPlatform() {
        return platform;
    }

    public String getUserAgent() {
        return userAgent;
    }
}
