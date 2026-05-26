package br.edu.acad.ifma.device.domain;

public class DuplicateDeviceTokenException extends RuntimeException {
    public DuplicateDeviceTokenException(String message) {
        super(message);
    }
}
