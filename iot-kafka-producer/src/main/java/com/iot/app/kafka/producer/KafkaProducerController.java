package com.iot.app.kafka.producer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class KafkaProducerController {
	
	private static final Log LOG = LogFactory.getLog(KafkaProducerController.class);
	
	@Autowired
	EventGenerator eventGenerator;

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = {"/", "/index"})
	public String index(Model model) {
		LOG.info(String.format("Event producer is running = [%s]", eventGenerator.isRunning()));
		model.addAttribute("isRunning", eventGenerator.isRunning());
		return "index";
	}

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = {"/run", "/start"})
	public String runProducer(Model model) {
		LOG.info("Starting event producer...");
		eventGenerator.start();
		model.addAttribute("isRunning", Boolean.TRUE);
		model.addAttribute("result", "Event producer STARTED.");
		return "index";
	}

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = {"/stop"})
	public String stopProducer(Model model) {
		LOG.info("Stopping event producer...");
		eventGenerator.stop();
		model.addAttribute("isRunning", Boolean.FALSE);
		model.addAttribute("result", "Event producer STOPPED.");
		return "index";
	}
}
