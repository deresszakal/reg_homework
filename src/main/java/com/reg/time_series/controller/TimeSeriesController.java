package com.reg.time_series.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.reg.time_series.entity.TimeSeries;



/**
 * @author phars
 * 
 */

@RestController
@RequestMapping("/timeseries")
public class TimeSeriesController {
	
	public static final String MSG_DATA_NOT_PARSERABLE = "Data not parserable";
	public static final String MSG_NO_DATA_RECEIVED = "No data received.";
	
	private static final Logger logger = LoggerFactory.getLogger(TimeSeriesController.class);
	
	@PutMapping(value = "/upload", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> uploadTimeSeries(@RequestBody String jsonString) {
		boolean resultOK = true;
		ResponseEntity<String> responseEntity = null;
		String message = "";
		if (jsonString != null && !jsonString.isEmpty()) {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.registerModule(new JavaTimeModule());
			TimeSeries timeSeries = null;
			try {
				timeSeries = objectMapper.readValue(jsonString, TimeSeries.class);
				resultOK = true;
				message = timeSeries.toString();
			} catch (JsonProcessingException e) {
				logger.debug("Json parsing error:", e);
				resultOK = false;
				message = MSG_DATA_NOT_PARSERABLE + ": " + (e.getCause() == null ? e.getMessage().substring(0, e.getMessage().indexOf("(")) : e.getCause().toString());
			}
		} else {
			resultOK = false;
			message = MSG_NO_DATA_RECEIVED;
		}
		if (resultOK) {
			responseEntity = ResponseEntity.ok().body(message);
		} else {
			responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
			logger.info(message);
			
		}
		return responseEntity;
	}

    @PutMapping(value ="/uploadmultipart", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<Void> uploadTimeSeries(@RequestParam("file") MultipartFile file) {
    	String jsonString = null;
    	if (!file.isEmpty()) {
    	    byte[] bytes = null;
			try {
				bytes = file.getBytes();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	    jsonString = new String(bytes);
    	};
    	ObjectMapper objectMapper = new ObjectMapper();
    	objectMapper.registerModule(new JavaTimeModule());
    	TimeSeries timeSeries = null;
    	try {
			timeSeries = objectMapper.readValue(jsonString, TimeSeries.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
        return ResponseEntity.ok().build();
    }

	
}
