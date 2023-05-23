package com.iot.app.kafka.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.app.kafka.vo.IoTData;

import kafka.serializer.Encoder;
import kafka.utils.VerifiableProperties;

/**
 * Class to convert IoTData java object to JSON String
 * 
 * @author abaghel
 *
 */
public class IoTDataEncoder implements Encoder<IoTData> {
	
	private static final Log LOG = LogFactory.getLog(IoTDataEncoder.class);	
	private static ObjectMapper objectMapper = new ObjectMapper();		
	public IoTDataEncoder(VerifiableProperties verifiableProperties) {

    }
	public byte[] toBytes(IoTData iotEvent) {
		try {
			String msg = objectMapper.writeValueAsString(iotEvent);
			LOG.info(msg);
			return msg.getBytes();
		} catch (JsonProcessingException e) {
			LOG.error("Error in Serialization", e);
		}
		return null;
	}
}
