package br.edu.acad.ifma.device.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String DEVICE_EXCHANGE = "device.exchange";
    public static final String DEVICE_REGISTERED_QUEUE = "device.registered.queue";
    public static final String DEVICE_REGISTERED_ROUTING_KEY = "device.registered";

    @Bean
    public TopicExchange deviceExchange() {
        return new TopicExchange(DEVICE_EXCHANGE, true, false);
    }

    @Bean
    public Queue deviceRegisteredQueue() {
        return QueueBuilder.durable(DEVICE_REGISTERED_QUEUE).build();
    }

    @Bean
    public Binding deviceRegisteredBinding(Queue deviceRegisteredQueue, TopicExchange deviceExchange) {
        return BindingBuilder.bind(deviceRegisteredQueue).to(deviceExchange).with(DEVICE_REGISTERED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
