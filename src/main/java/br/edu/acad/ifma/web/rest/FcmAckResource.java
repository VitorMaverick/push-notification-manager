package br.edu.acad.ifma.web.rest;

import br.edu.acad.ifma.app.port.NotificationRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/fcm")
public class FcmAckResource {

    private final Logger log = LoggerFactory.getLogger(FcmAckResource.class);
    private final NotificationRepositoryPort notificationRepository;

    public FcmAckResource(NotificationRepositoryPort notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @PostMapping("/ack")
    public ResponseEntity<Void> ack(@RequestBody FcmAckRequest request) {
        log.info(
            "Received FCM ack: messageId={} token={} receivedAt={}",
            request.getMessageId(),
            request.getToken(),
            request.getReceivedAt()
        );
        if (request.getMessageId() != null) {
            notificationRepository
                .findByFcmMessageId(request.getMessageId())
                .ifPresent(n -> {
                    n.markDelivered();
                    notificationRepository.save(n);
                });
        }
        return ResponseEntity.ok().build();
    }
}
