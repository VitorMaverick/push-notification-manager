package br.edu.acad.ifma.notification.domain;

public class PushSendingException extends RuntimeException {
    public PushSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
