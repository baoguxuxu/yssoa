package com.lebaoxun.soa.amqp.core;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;

@Configuration
public class AmqpConfig {

	@Bean
	RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
		return new RabbitAdmin(connectionFactory);
	}

	/**
	 * 生产者用
	 * 
	 * @return
	 */
	@Bean
	public RabbitMessagingTemplate rabbitMessagingTemplate(RabbitTemplate rabbitTemplate) {
		RabbitMessagingTemplate rabbitMessagingTemplate = new RabbitMessagingTemplate();
		rabbitMessagingTemplate.setMessageConverter(jackson2Converter());
		rabbitMessagingTemplate.setRabbitTemplate(rabbitTemplate);
		return rabbitMessagingTemplate;
	}

	@Bean
	public MappingJackson2MessageConverter jackson2Converter() {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		return converter;
	}

}