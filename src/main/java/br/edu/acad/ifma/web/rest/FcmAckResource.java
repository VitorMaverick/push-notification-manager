package br.edu.acad.ifma.web.rest;

import br.edu.acad.ifma.domain.Notification;
import br.edu.acad.ifma.repository.NotificationMessageRepository;
import java.time.Instant;
import java.util.Optional;
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

    private final NotificationMessageRepository repository;

    public FcmAckResource(NotificationMessageRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/ack")
    public ResponseEntity<Void> ack(@RequestBody FcmAckRequest request) {
        log.info(
            "Received FCM ack: messageId={} token={} receivedAt={}",
            request.getMessageId(),
            request.getToken(),
            request.getReceivedAt()
        );
        // Attempt to find the notification by fcmMessageId
        if (request.getMessageId() != null) {
            Optional<Notification> opt = repository
                .findAll() // fallback: search by fcmMessageId
                .stream()
                .filter(n -> request.getMessageId().equals(n.getFcmMessageId()))
                .findFirst();
            if (opt.isPresent()) {
                Notification n = opt.get();
                n.setDeliveredAt(request.getReceivedAt() == null ? Instant.now() : request.getReceivedAt());
                repository.save(n);
                log.info("Marked notification id={} as deliveredAt={}", n.getId(), n.getDeliveredAt());
                return ResponseEntity.ok().build();
            }
        }

        // Fallback: try to match by token and recent create time (last 5 minutes)
        if (request.getToken() != null) {
            Instant cutoff = Instant.now().minusSeconds(300);
            Optional<Notification> opt2 = repository
                .findAll()
                .stream()
                .filter(n -> request.getToken().equals(n.getRecipientToken()) && n.getCreatedAt().isAfter(cutoff))
                .findFirst();
            if (opt2.isPresent()) {
                Notification n = opt2.get();
                n.setDeliveredAt(request.getReceivedAt() == null ? Instant.now() : request.getReceivedAt());
                repository.save(n);
                log.info("Marked notification id={} as deliveredAt={} (matched by token)", n.getId(), n.getDeliveredAt());
                return ResponseEntity.ok().build();
            }
        }

        // nothing matched; just return OK
        return ResponseEntity.ok().build();
    }
}
