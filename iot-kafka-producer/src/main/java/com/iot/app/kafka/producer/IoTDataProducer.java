package com.iot.app.kafka.producer;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;

/**
 * IoT data event producer class which uses Kafka producer for events. 
 * 
 * @author abaghel
 *
 */
@SpringBootApplication
@ComponentScan(basePackages={"com.iot.app.kafka.producer"})
public class IoTDataProducer {

	@Autowired
	KafkaConfiguration kafkaConfiguration;

	public static void main(String[] args) throws Exception {
		SpringApplication.run(IoTDataProducer.class, args);
	}

	@Bean
	public String topicName() {
		return kafkaConfiguration.getTopic();
	}

	@Bean
	public NewTopic topic() {
		return new NewTopic(kafkaConfiguration.getTopic(), 1, (short) 1); 
	}

	@Bean
	public RecordMessageConverter converter() {
		return new JsonMessageConverter();
	}

	@Bean
	public EventGenerator eventGenerator() {
		return new EventGenerator();
	}

}
