package br.edu.acad.ifma.service.notification;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simple in-memory queue manager to accumulate tokens/messages and process them asynchronously.
 * For production use replace with a persistent queue (RabbitMQ/Kafka/SQS) for durability and scale.
 */
@Component
public class FcmQueueManager {

    private final Logger log = LoggerFactory.getLogger(FcmQueueManager.class);
    private final FcmService fcmService;

    private final Queue<QueueItem> queue = new ConcurrentLinkedQueue<>();

    public FcmQueueManager(FcmService fcmService) {
        this.fcmService = fcmService;
    }

    public void enqueue(String token, MensagemEnviada mensagem) {
        queue.add(new QueueItem(token, mensagem));
    }

    public int processQueue() {
        int processed = 0;
        QueueItem item;
        while ((item = queue.poll()) != null) {
            try {
                fcmService.sendToToken(item.token, item.mensagem);
                processed++;
            } catch (Exception e) {
                log.error("Failed to process queue item token={}: {}", item.token, e.getMessage());
            }
        }
        log.info("Processed {} items from queue", processed);
        return processed;
    }

    private static final class QueueItem {

        final String token;
        final MensagemEnviada mensagem;

        QueueItem(String token, MensagemEnviada mensagem) {
            this.token = token;
            this.mensagem = mensagem;
        }
    }
}
