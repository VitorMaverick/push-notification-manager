package br.edu.acad.ifma.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.FileInputStream;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura e cria o bean FirebaseMessaging quando a propriedade
 * `firebase.service-account-file` estiver definida (caminho para o JSON de service account).
 */
@Configuration
public class FirebaseConfig {

    private final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.service-account-file:}")
    private String serviceAccountFile;

    @Bean
    @ConditionalOnProperty(name = "firebase.service-account-file")
    public FirebaseMessaging firebaseMessaging() throws Exception {
        log.info("Initializing FirebaseMessaging from service account file={}", serviceAccountFile);
        try (InputStream serviceAccount = new FileInputStream(serviceAccountFile)) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
            FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).build();
            FirebaseApp app = FirebaseApp.initializeApp(options, "PushNotificationManager");
            log.info("Firebase initialized (app name={})", app.getName());
            return FirebaseMessaging.getInstance(app);
        } catch (Exception e) {
            log.error("Failed to initialize FirebaseMessaging: {}", e.getMessage());
            throw e;
        }
    }
}
