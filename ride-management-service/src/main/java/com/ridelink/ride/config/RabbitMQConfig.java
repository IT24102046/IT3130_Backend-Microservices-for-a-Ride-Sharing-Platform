package com.ridelink.ride.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String RIDE_EXCHANGE = "ride.exchange";
    public static final String RIDE_ASSIGNED_QUEUE = "ride.assigned.queue";
    public static final String RIDE_STATUS_QUEUE = "ride.status.queue";
    public static final String RIDE_ASSIGNED_ROUTING_KEY = "ride.assigned";
    public static final String RIDE_STATUS_ROUTING_KEY = "ride.status.changed";
    
    @Bean
    public TopicExchange rideExchange() {
        return new TopicExchange(RIDE_EXCHANGE, true, false);
    }
    
    @Bean
    public Queue rideAssignedQueue() {
        return new Queue(RIDE_ASSIGNED_QUEUE, true);
    }
    
    @Bean
    public Queue rideStatusQueue() {
        return new Queue(RIDE_STATUS_QUEUE, true);
    }
    
    @Bean
    public Binding rideAssignedBinding(Queue rideAssignedQueue, TopicExchange rideExchange) {
        return BindingBuilder.bind(rideAssignedQueue).to(rideExchange).with(RIDE_ASSIGNED_ROUTING_KEY);
    }
    
    @Bean
    public Binding rideStatusBinding(Queue rideStatusQueue, TopicExchange rideExchange) {
        return BindingBuilder.bind(rideStatusQueue).to(rideExchange).with(RIDE_STATUS_ROUTING_KEY);
    }
    
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}