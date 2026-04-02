package br.edu.acad.ifma.web.rest;

import br.edu.acad.ifma.domain.Notification;
import br.edu.acad.ifma.domain.NotificationChannel;
import br.edu.acad.ifma.service.notification.NotificationService;
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
    private final NotificationService notificationService;

    public FcmResource(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendPush(@Valid @RequestBody FcmRequest request) {
        log.debug("Received FCM send request for token={}", request.getToken());
        Notification nm = new Notification(request.getTitulo(), request.getCorpo(), NotificationChannel.FCM_PUSH);
        nm.setRecipientToken(request.getToken());
        notificationService.createAndSend(nm);
        return ResponseEntity.accepted().body("Queued via NotificationService");
    }
}
