package br.edu.acad.ifma.web.rest;

import java.time.Instant;

public class FcmAckRequest {

    private String messageId;
    private String token;
    private Instant receivedAt;

    public FcmAckRequest() {}

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }
}
