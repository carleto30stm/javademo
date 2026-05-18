package com.productos.demo.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_RATE_QUEUE    = "exchange-rate-alerts";
    public static final String EXCHANGE_RATE_EXCHANGE = "exchange-rate-events";
    public static final String EXCHANGE_RATE_ROUTING  = "rate.change.alert";

    @Bean
    public Queue exchangeRateQueue() {
        return new Queue(EXCHANGE_RATE_QUEUE, true);
    }

    @Bean
    public TopicExchange exchangeRateExchange() {
        return new TopicExchange(EXCHANGE_RATE_EXCHANGE);
    }

    @Bean
    public Binding exchangeRateBinding(Queue exchangeRateQueue, TopicExchange exchangeRateExchange) {
        return BindingBuilder
            .bind(exchangeRateQueue)
            .to(exchangeRateExchange)
            .with(EXCHANGE_RATE_ROUTING);
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         JacksonJsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
