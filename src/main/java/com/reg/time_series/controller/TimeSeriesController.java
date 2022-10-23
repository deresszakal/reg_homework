package com.reg.time_series.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.reg.time_series.entity.TimeSeries;
import com.reg.time_series.entity.TimeSeriesRepository;
import com.reg.time_series.sevice.ApplicationService;
import com.reg.time_series.sevice.TimeSeriesService;


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
	public static final String NAME_UNIQUEPOWERSTATIONDATETIMESTAMP_C = "UNIQUE_POWERSTATION_DATE_TIMESTAMP";
	public static final String MSG_UNIQUEPOWERSTATIONDATETIMESTAMP_CV = "TIME_SERIES(POWERSTATION, DATE, TIMESTAMP) Constraint Violation";
	public static final String NAME_UNIQUEPOWERSTATIONDATEVERSION_C = "UNIQUE_POWERSTATION_DATE_VERSION";
	public static final String MSG_UNIQUEPOWERSTATIONDATEVERSION_CV = "TIME_SERIES(POWERSTATION, DATE, VERSION) Constraint Violation";
	private static final Logger logger = LoggerFactory.getLogger(TimeSeriesController.class);
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	@Autowired
	private TimeSeriesRepository timeSeriesRepository;
	
	@Autowired
	TimeSeriesService timeSeriesService;
	
	@Autowired
	ApplicationService applicationService;

	@PutMapping(value = "/upload", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> uploadTimeSeries(@RequestBody String jsonString) {
		return uploadInner(jsonString);
	}
	
    @PutMapping(value ="/uploadmultipart", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> uploadTimeSeries(@RequestParam("file") MultipartFile file) {
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
    	return uploadInner(jsonString);
    }
    
	public ResponseEntity<String> uploadInner(String jsonString) {
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
					if (timeSeries.getVersion() == 0 ) {
						int newVersion = timeSeriesService.getFreeVersionNumber(timeSeries.getPowerStation(), timeSeries.getDate());
						timeSeries.setVersion(newVersion);
					}
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
				if (e2.getMessage().toLowerCase().contains(NAME_UNIQUEPOWERSTATIONDATETIMESTAMP_C.toLowerCase())) {
					message = MSG_UNIQUEPOWERSTATIONDATETIMESTAMP_CV + ": " + timeSeries.toString() + ", " + timeSeries.getTimestamp();
				}
				else if (e2.getMessage().toLowerCase().contains(NAME_UNIQUEPOWERSTATIONDATEVERSION_C.toLowerCase())) {
					message = MSG_UNIQUEPOWERSTATIONDATEVERSION_CV + ": " + timeSeries.toString() + ", " + timeSeries.getTimestamp();
				}
				message += ": " + timeSeries.toString() + ", " + timeSeries.getTimestamp();
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

    @GetMapping("/powerstations")
    public List<TimeSeries> getPowerstations() {
    	Optional<List<TimeSeries>> sqlResult = timeSeriesRepository.findGroupByPowerStation();
    	List<TimeSeries> result = null;
    	if (sqlResult.isPresent()) {
			result = sqlResult.get();
		} else {
			result = Collections.emptyList();
		}
    	return result;
	}

    @GetMapping("/datesbypowerstation")
    @ResponseBody
    public List<TimeSeries> getDatesByPowerstation(@RequestParam(name = "powerstation") String powerStation) {
    	Optional<List<TimeSeries>> sqlResult = timeSeriesRepository.findPowerStationByPowerStationGroupByDate(powerStation);
    	List<TimeSeries> result = null;
    	if (sqlResult.isPresent()) {
    		result = sqlResult.get();
    	} else {
    		result = Collections.emptyList();
    	}
    	return result;
    }
    
    @GetMapping("/seriesbypowerstationanddate")
    @ResponseBody
    public List<TimeSeries> getSeriesByPowerstationAndDate(@RequestParam(name = "powerstation") String powerStation, @RequestParam(name = "date") String dateString ) {
    	LocalDate date = LocalDate.parse(dateString, dateFormatter);
    	Optional<List<TimeSeries>> sqlResult = timeSeriesRepository.findByPowerStationAndDate(powerStation, date);
    	List<TimeSeries> result = null;
    	if (sqlResult.isPresent()) {
    		result = sqlResult.get();
    		result = timeSeriesService.mergeTimeseriesData(result);
    	} else {
    		result = Collections.emptyList();
    	}
    	return result;
    }
    
}
