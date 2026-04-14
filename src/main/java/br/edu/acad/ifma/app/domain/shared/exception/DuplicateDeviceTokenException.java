package br.edu.acad.ifma.app.domain.shared.exception;

public class DuplicateDeviceTokenException extends DomainException {

    public DuplicateDeviceTokenException(String message) {
        super(message);
    }
}
