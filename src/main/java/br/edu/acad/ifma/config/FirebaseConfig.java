package br.edu.acad.ifma.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura e cria o bean FirebaseMessaging quando a propriedade
 * `firebase.service-account-file` estiver definida (caminho para o JSON de service account).
 *
 * Suporta valores como:
 * - classpath:firebase-key.json  -> carrega de src/main/resources
 * - /absolute/path/to/firebase-key.json
 * - relative/path/firebase-key.json
 */
@Configuration
public class FirebaseConfig {

    private final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.service-account-file:}")
    private String serviceAccountFile;

    private static final String FIREBASE_APP_NAME = "push-notification-manager";

    @Bean
    @ConditionalOnProperty(name = "firebase.service-account-file")
    public FirebaseMessaging firebaseMessaging() throws Exception {
        if (serviceAccountFile == null || serviceAccountFile.isBlank()) {
            throw new IllegalStateException("firebase.service-account-file is empty");
        }

        log.info("Initializing FirebaseMessaging from service account file={}", serviceAccountFile);

        // Reuse existing app if already initialized
        Optional<FirebaseApp> existing = FirebaseApp.getApps()
            .stream()
            .filter(a -> FIREBASE_APP_NAME.equals(a.getName()))
            .findFirst();

        if (existing.isPresent()) {
            log.info("FirebaseApp '{}' already initialized, reusing instance", FIREBASE_APP_NAME);
            return FirebaseMessaging.getInstance(existing.get());
        }

        try (InputStream serviceAccount = openServiceAccountStream(serviceAccountFile)) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
            FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).build();
            FirebaseApp app = FirebaseApp.initializeApp(options, FIREBASE_APP_NAME);
            log.info("Firebase initialized (app name={})", app.getName());
            return FirebaseMessaging.getInstance(app);
        } catch (Exception e) {
            log.error("Failed to initialize FirebaseMessaging: {}", e.getMessage(), e);
            throw e;
        }
    }

    private InputStream openServiceAccountStream(String path) throws Exception {
        final String prefix = "classpath:";
        if (path.startsWith(prefix)) {
            String resourcePath = path.substring(prefix.length());
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            if (is == null) {
                throw new IllegalArgumentException("Resource not found on classpath: " + resourcePath);
            }
            return is;
        }
        // Fallback to file system path
        return new FileInputStream(path);
    }
}
