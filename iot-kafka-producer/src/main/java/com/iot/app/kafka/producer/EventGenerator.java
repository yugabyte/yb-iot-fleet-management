package com.iot.app.kafka.producer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

public class EventGenerator implements Runnable {

	public static final Log LOG = LogFactory.getLog(EventGenerator.class);

	private Thread worker;

	@Autowired
	KafkaTemplate<String, IoTData> kafkaTemplate;

	//@Autowired
	//Producer<String, IoTData> producer;

	@Autowired
	String topicName;

	private AtomicBoolean running = new AtomicBoolean(false);

	public Boolean isRunning() {
		return running.get();
	}

	public void start() {
		worker = new Thread(this);
		worker.start();
	}

	public void stop() {
		running.set(false);
	}

	public void interrupt() {
		running.set(false);
		worker.interrupt();
	}

	public void run() {
		LOG.info("Starting event generation.");
		running.set(true);
		List<String> routeList = Arrays.asList(new String[] { "Route-37", "Route-43", "Route-82" });
		List<String> vehicleTypeList = Arrays
				.asList(new String[] { "Large Truck", "Small Truck", "Van", "18 Wheeler", "Car" });
		Random rand = new Random();
		// generate event in loop
		while (isRunning()) {
			LOG.info(String.format("Thread is running = [%s]", isRunning()));
			List<IoTData> eventList = new ArrayList<IoTData>();
			for (int i = 0; i < 100; i++) {// create 100 vehicles
				if (!isRunning())
					break;
				String vehicleId = UUID.randomUUID().toString();
				String vehicleType = vehicleTypeList.get(rand.nextInt(5));
				String routeId = routeList.get(rand.nextInt(3));
				double speed = rand.nextInt(100 - 20) + 20;// random speed between 20 to 100
				double fuelLevel = rand.nextInt(40 - 10) + 10;
				for (int j = 0; j < 5; j++) {// Add 5 events for each vehicle
					String coords = getCoordinates(routeId);
					String latitude = coords.substring(0, coords.indexOf(","));
					String longitude = coords.substring(coords.indexOf(",") + 1, coords.length());
					// The timestamp field is set during event submission to get different values
					// across events.
					IoTData event = new IoTData(vehicleId, vehicleType, routeId, latitude, longitude, null, speed,
							fuelLevel);
					eventList.add(event);
				}
			}
			Collections.shuffle(eventList);// shuffle for random events
			for (IoTData event : eventList) {
				if (!isRunning())
					break;
				event.setTimestamp(new Date());
				LOG.info(String.format("Sending event: {%s}", event));
				kafkaTemplate.send(topicName, event);
				try {
					Thread.sleep(rand.nextInt(1000 - 500) + 500);
				} catch (InterruptedException e) {
					LOG.error("Error sleeping thread.", e);
					e.printStackTrace();
					break;
				} // random delay of 0.5 to 1 second
			}
		}
	}

	// Method to generate random latitude and longitude for routes
	private String getCoordinates(String routeId) {
		Random rand = new Random();
		int latPrefix = 0;
		int longPrefix = -0;
		if (routeId.equals("Route-37")) {
			latPrefix = 33;
			longPrefix = -96;
		} else if (routeId.equals("Route-82")) {
			latPrefix = 34;
			longPrefix = -97;
		} else if (routeId.equals("Route-43")) {
			latPrefix = 35;
			longPrefix = -98;
		}
		Float lati = latPrefix + rand.nextFloat();
		Float longi = longPrefix + rand.nextFloat();
		return lati + "," + longi;
	}
}
