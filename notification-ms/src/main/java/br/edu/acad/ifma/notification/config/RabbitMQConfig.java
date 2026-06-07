package br.edu.acad.ifma.notification.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Device side (consumer) ──────────────────────────────────────────────
    public static final String DEVICE_EXCHANGE              = "device.exchange";
    public static final String DEVICE_REGISTERED_QUEUE      = "device.registered.queue";
    public static final String DEVICE_REGISTERED_ROUTING_KEY = "device.registered";

    // ── Notification retry topology ─────────────────────────────────────────
    public static final String NOTIFICATION_RETRY_EXCHANGE          = "notification.retry.exchange";
    public static final String NOTIFICATION_RETRY_QUEUE             = "notification.retry.queue";
    public static final String NOTIFICATION_RETRY_PROCESS_QUEUE     = "notification.retry.process.queue";
    public static final String NOTIFICATION_DLQ                     = "notification.dlq";

    public static final String ROUTING_RETRY                = "retry";
    public static final String ROUTING_RETRY_PROCESS        = "retry.process";
    public static final String ROUTING_RETRY_DLQ            = "retry.dlq";

    public static final int    RETRY_TTL_MS                 = 15_000;
    public static final int    MAX_RETRY_ATTEMPTS           = 3;

    // ── Exchanges ────────────────────────────────────────────────────────────
    @Bean
    public TopicExchange deviceExchange() {
        return new TopicExchange(DEVICE_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange notificationRetryExchange() {
        return new DirectExchange(NOTIFICATION_RETRY_EXCHANGE, true, false);
    }

    // ── Queues ───────────────────────────────────────────────────────────────
    @Bean
    public Queue deviceRegisteredQueue() {
        return QueueBuilder.durable(DEVICE_REGISTERED_QUEUE).build();
    }

    /** Delay queue: messages sit here for RETRY_TTL_MS then move to retry.process */
    @Bean
    public Queue notificationRetryQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", RETRY_TTL_MS);
        args.put("x-dead-letter-exchange", NOTIFICATION_RETRY_EXCHANGE);
        args.put("x-dead-letter-routing-key", ROUTING_RETRY_PROCESS);
        return QueueBuilder.durable(NOTIFICATION_RETRY_QUEUE).withArguments(args).build();
    }

    /** Worker queue: retry consumer processes messages from here */
    @Bean
    public Queue notificationRetryProcessQueue() {
        return QueueBuilder.durable(NOTIFICATION_RETRY_PROCESS_QUEUE).build();
    }

    /** Final dead-letter queue for permanently failed notifications */
    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }

    // ── Bindings ─────────────────────────────────────────────────────────────
    @Bean
    public Binding deviceRegisteredBinding(Queue deviceRegisteredQueue, TopicExchange deviceExchange) {
        return BindingBuilder.bind(deviceRegisteredQueue).to(deviceExchange).with(DEVICE_REGISTERED_ROUTING_KEY);
    }

    @Bean
    public Binding retryBinding(Queue notificationRetryQueue, DirectExchange notificationRetryExchange) {
        return BindingBuilder.bind(notificationRetryQueue).to(notificationRetryExchange).with(ROUTING_RETRY);
    }

    @Bean
    public Binding retryProcessBinding(Queue notificationRetryProcessQueue, DirectExchange notificationRetryExchange) {
        return BindingBuilder.bind(notificationRetryProcessQueue).to(notificationRetryExchange).with(ROUTING_RETRY_PROCESS);
    }

    @Bean
    public Binding dlqBinding(Queue notificationDlq, DirectExchange notificationRetryExchange) {
        return BindingBuilder.bind(notificationDlq).to(notificationRetryExchange).with(ROUTING_RETRY_DLQ);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
