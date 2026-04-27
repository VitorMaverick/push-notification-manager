package br.edu.acad.ifma.adapters.auth.web.rest.internal;

import com.google.firebase.messaging.FirebaseMessaging;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/firebase")
public class FirebaseHealthResource {

    private final Logger log = LoggerFactory.getLogger(FirebaseHealthResource.class);

    private final Optional<FirebaseMessaging> firebaseMessaging;

    public FirebaseHealthResource(Optional<FirebaseMessaging> firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new HashMap<>();
        try {
            if (this.firebaseMessaging.isEmpty()) {
                body.put("status", "DOWN");
                body.put("reason", "FirebaseMessaging bean is not available");
                log.warn("FirebaseMessaging bean not available");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
            }
            // If bean exists, return UP. We can't call remote FCM here; just existence check.
            body.put("status", "UP");
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            body.put("status", "DOWN");
            body.put("reason", e.getMessage());
            log.error("Error checking Firebase health: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
        }
    }
}
