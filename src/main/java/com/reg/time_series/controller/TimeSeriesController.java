package com.reg.time_series.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import javax.persistence.PersistenceException;
import javax.persistence.PrePersist;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
import com.reg.time_series.entity.TimeSeriesRepository;
import com.reg.time_series.sevice.TimeSeriesService;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;


/**
 * @author phars
 * 
 */

@RestController
@RequestMapping("/timeseries")
public class TimeSeriesController {
	
	public static final String MSG_DATA_NOT_PARSERABLE = "Data not parserable";
	public static final String MSG_NO_DATA_RECEIVED = "No data received.";
	public static final String MSG_DATA_NOT_NEW = "Data not new";
	public static final String MSG_UNIQUEPOWERSTATIONDATETIMESTAMP_CV = "TIME_SERIES(POWERSTATION, DATE, TIMESTAMP) Constraint Violation";
	private static final Logger logger = LoggerFactory.getLogger(TimeSeriesController.class);
	
	@Autowired
	private TimeSeriesRepository timeSeriesRepository;
	
	@Autowired
	TimeSeriesService timeSeriesService;
	
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
				if (timeSeriesService.isTimeSeriesNewer(timeSeries)) {
					resultOK = true;
					int newVersion = timeSeriesService.getFreeVersionNumber(timeSeries.getPowerStation(), timeSeries.getDate());
					timeSeries.setVersion(newVersion);
					timeSeries = timeSeriesRepository.save(timeSeries);
					message = timeSeries.toString();
				} else {
					resultOK = false;
					message = MSG_DATA_NOT_NEW + ": " + timeSeries.toString() + ", " + timeSeries.getTimestamp();
				}
			} catch (JsonProcessingException e1) {
				logger.debug("Json parsing error:", e1);
				resultOK = false;
				message = MSG_DATA_NOT_PARSERABLE + ": " + (e1.getCause() == null ? e1.getMessage().substring(0, e1.getMessage().indexOf("(")) : e1.getCause().toString());
			} catch (DataIntegrityViolationException e2) {
				resultOK = false;
				message = MSG_UNIQUEPOWERSTATIONDATETIMESTAMP_CV + ": " + timeSeries.toString() + ", " + timeSeries.getTimestamp();
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
