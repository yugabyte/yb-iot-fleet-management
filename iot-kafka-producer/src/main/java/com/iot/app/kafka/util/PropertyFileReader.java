package com.iot.app.kafka.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class to read property file
 * 
 * @author abaghel
 *
 */
public class PropertyFileReader {
	private static final Log LOG = LogFactory.getLog(PropertyFileReader.class);
	private static Properties prop = new Properties();
	public static Properties readPropertyFile() throws Exception {
		if (prop.isEmpty()) {
			InputStream input = PropertyFileReader.class.getClassLoader().getResourceAsStream("iot-kafka.properties");
			try {
				prop.load(input);
			} catch (IOException ex) {
				LOG.error(ex);
				throw ex;
			} finally {
				if (input != null) {
					input.close();
				}
			}
		}
		return prop;
	}
}
