package com.reg.time_series.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
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
	
	
  @PutMapping(value ="/upload", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
  public ResponseEntity<Void> uploadTimeSeries(@RequestBody String jsonString) {
	if(jsonString != null && !jsonString.isEmpty()) {  
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
	else {
		return ResponseEntity.badRequest().build();
	}
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
