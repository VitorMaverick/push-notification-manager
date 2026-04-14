package br.edu.acad.ifma.app.usecase.notification;

public class SendPushNotificationCommand {

    private final String deviceToken;
    private final String title;
    private final String body;

    public SendPushNotificationCommand(String deviceToken, String title, String body) {
        this.deviceToken = deviceToken;
        this.title = title;
        this.body = body;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }
}
