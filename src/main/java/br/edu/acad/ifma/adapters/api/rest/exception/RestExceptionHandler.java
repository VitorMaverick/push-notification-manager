package br.edu.acad.ifma.adapters.api.rest.exception;

import br.edu.acad.ifma.adapters.api.rest.outbound.ResponseError;
import br.edu.acad.ifma.app.domain.shared.exception.DeviceNotFoundException;
import br.edu.acad.ifma.app.domain.shared.exception.DomainException;
import br.edu.acad.ifma.app.domain.shared.exception.DuplicateDeviceTokenException;
import br.edu.acad.ifma.app.domain.shared.exception.InvalidFcmTokenException;
import br.edu.acad.ifma.app.domain.shared.exception.NotificationNotFoundException;
import br.edu.acad.ifma.app.domain.shared.exception.PushSendingException;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(InvalidFcmTokenException.class)
    public ResponseEntity<ResponseError> handle(InvalidFcmTokenException e) {
        return ResponseEntity.badRequest().body(new ResponseError(400, e.getMessage()));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ResponseError> handle(DomainException e) {
        return ResponseEntity.badRequest().body(new ResponseError(400, e.getMessage()));
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ResponseError> handle(NotificationNotFoundException e) {
        return ResponseEntity.status(404).body(new ResponseError(404, e.getMessage()));
    }

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<ResponseError> handle(DeviceNotFoundException e) {
        return ResponseEntity.status(404).body(new ResponseError(404, e.getMessage()));
    }

    @ExceptionHandler(DuplicateDeviceTokenException.class)
    public ResponseEntity<ResponseError> handle(DuplicateDeviceTokenException e) {
        return ResponseEntity.status(409).body(new ResponseError(409, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseError> handle(MethodArgumentNotValidException e) {
        String msg = e
            .getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fe -> "Field " + fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(new ResponseError(400, msg));
    }

    @ExceptionHandler(PushSendingException.class)
    public ResponseEntity<ResponseError> handle(PushSendingException e) {
        return ResponseEntity.status(500).body(new ResponseError(500, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseError> handleGeneric(Exception e) {
        return ResponseEntity.status(500).body(new ResponseError(500, "Internal server error"));
    }
}
