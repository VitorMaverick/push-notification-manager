package br.edu.acad.ifma.web.rest;

import br.edu.acad.ifma.app.usecase.notification.SendPushNotificationCommand;
import br.edu.acad.ifma.app.usecase.notification.SendPushNotificationUseCase;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fcm")
@Validated
public class FcmResource {

    private final Logger log = LoggerFactory.getLogger(FcmResource.class);
    private final SendPushNotificationUseCase sendUseCase;

    public FcmResource(SendPushNotificationUseCase sendUseCase) {
        this.sendUseCase = sendUseCase;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendPush(@Valid @RequestBody FcmRequest request) {
        log.debug("Received FCM send request for token={}", request.getToken());
        SendPushNotificationCommand cmd = new SendPushNotificationCommand(request.getToken(), request.getTitulo(), request.getCorpo());
        sendUseCase.execute(cmd);
        return ResponseEntity.accepted().body("Queued via NotificationService");
    }
}
