package br.edu.acad.ifma.adapters.fcm;

/**
 * Runtime exception representing failures when calling the FCM client.
 */
public class FcmClientException extends RuntimeException {

    public FcmClientException(String message) {
        super(message);
    }

    public FcmClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
