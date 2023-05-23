package com.iot.app.kafka.producer;

import java.util.Collections;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.iot.app.kafka.util.EventGenerator;
import com.iot.app.kafka.util.PropertyFileReader;
import com.iot.app.kafka.vo.IoTData;

import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;

/**
 * IoT data event producer class which uses Kafka producer for events. 
 * 
 * @author abaghel
 *
 */
@SpringBootApplication
@ComponentScan(basePackages={"com.iot.app.kafka.controller", "com.iot.app.kafka.util"})
public class IoTDataProducer {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(IoTDataProducer.class, args);
	}

	@Bean
	public String topicName() {
		Properties prop = null;
		try {
			prop = PropertyFileReader.readPropertyFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prop.getProperty("com.iot.app.kafka.topic");
	}

	@Bean
	public Producer<String, IoTData> producer() {
		//read config file
		Properties prop = null;
		try {
			prop = PropertyFileReader.readPropertyFile();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		String zookeeper = prop.getProperty("com.iot.app.kafka.zookeeper");
		if (System.getProperty("com.iot.app.kafka.zookeeper") != null) {
					zookeeper = System.getProperty("com.iot.app.kafka.zookeeper");
		}
		String brokerList = prop.getProperty("com.iot.app.kafka.brokerlist");
		if (System.getProperty("com.iot.app.kafka.brokerlist") != null) {
					brokerList  = System.getProperty("com.iot.app.kafka.brokerlist");
		}

		// set producer properties
		Properties properties = new Properties();
		properties.put("zookeeper.connect", zookeeper);
		properties.put("metadata.broker.list", brokerList);
		properties.put("request.required.acks", "1");
		properties.put("serializer.class", "com.iot.app.kafka.util.IoTDataEncoder");
		Producer<String, IoTData> producer = new Producer<String, IoTData>(new ProducerConfig(properties));

		return producer;
		
	}

	@Bean
	public EventGenerator eventGenerator() {
		return new EventGenerator();
	}

}
