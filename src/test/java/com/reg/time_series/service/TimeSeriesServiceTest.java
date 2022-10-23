package com.reg.time_series.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.reg.time_series.entity.TimeSeries;
import com.reg.time_series.entity.TimeSeriesRepository;
import com.reg.time_series.sevice.ApplicationService;
import com.reg.time_series.sevice.TimeSeriesService;

import io.restassured.RestAssured;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TimeSeriesServiceTest {

    @LocalServerPort
    int port;

    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private TimeSeriesRepository timeSeriesRepository;
    
    @Autowired
    TimeSeriesService timeSeriesService;

    @Autowired
    ApplicationService aplApplicationService;
    
    @BeforeEach
    public void initAssuredMockMvcWebApplicationContext() {
        RestAssured.port = port;
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
    }

    @Test
    @Order(0)
    public void clean() {
		Optional<List<TimeSeries>> finded = timeSeriesRepository.findByDateBetween(
				LocalDate.parse("1999-01-01", TimeSeriesService.dateFormatter), 
				LocalDate.parse("1999-12-31", TimeSeriesService.dateFormatter));
		timeSeriesRepository.deleteAll(finded.get());
    }
    
    @Test
    @Order(1)
	public void nextVersionNotExistTimeSeries() throws JsonMappingException, JsonProcessingException {
		TimeSeries timeSeries = createTimeSeriesFromJsonString(jsonString);
		int newVersion = timeSeriesService.getFreeVersionNumber(timeSeries.getPowerStation(), timeSeries.getDate());
		timeSeries.setVersion(newVersion);
		timeSeries = timeSeriesRepository.save(timeSeries);
		assertThat(timeSeries.getVersion()).isEqualTo(1);
	}

    @Test
    @Order(2)
    public void nextVersionExistOneTimeSeries() throws JsonMappingException, JsonProcessingException {
    	ObjectMapper objectMapper = new ObjectMapper();
    	objectMapper.registerModule(new JavaTimeModule());
    	TimeSeries timeSeries = null;
    	try {
    		timeSeries = addOneTimeSeries(objectMapper, "1999-06-28 14:12:24");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	assertThat(timeSeries.getVersion()).isEqualTo(2);
    }
    
    @Test
    @Order(2)
    public void nextVersionExistFourTimeSeries() throws JsonMappingException, JsonProcessingException {
    	ObjectMapper objectMapper = new ObjectMapper();
    	objectMapper.registerModule(new JavaTimeModule());
    	TimeSeries timeSeries = null;
    	try {
    		timeSeries = addOneTimeSeries(objectMapper, "1999-06-28 15:17:33");
    		timeSeries = addOneTimeSeries(objectMapper, "1999-06-28 17:45:12");
    		timeSeries = addOneTimeSeries(objectMapper, "1999-06-28 18:40:22");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	assertThat(timeSeries.getVersion()).isEqualTo(5);
    }

    @Test
    @Order(3)
    public void calculteSafetyWindowEnd_MiddleOfTheDay() {
	    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    	LocalDateTime start = LocalDateTime.parse("1999-06-28 13:29:53", dateTimeFormatter);
    	LocalDateTime end = LocalDateTime.parse("1999-06-28 15:00:00", dateTimeFormatter);
    	LocalDateTime safetyWindowEnd = timeSeriesService.calculateSafetyWindowEnd(start);
    	assertThat(safetyWindowEnd).isEqualTo(end);
    }

    @Test
    @Order(4)
    public void calculteSafetyWindowEnd_closeToMidnight() {
    	LocalDateTime start = LocalDateTime.parse("1999-06-28 23:29:53", TimeSeriesService.dateTimeFormatter);
    	LocalDateTime end = LocalDateTime.parse("1999-06-29 00:00:00", TimeSeriesService.dateTimeFormatter);
    	LocalDateTime safetyWindowEnd = timeSeriesService.calculateSafetyWindowEnd(start);
    	assertThat(safetyWindowEnd).isEqualTo(end);
    }
    
    @Test
    @Order(5)
    public void mergeTimeseriesData_oneTimeSeries() {
    	ArrayList<TimeSeries> inputList = new ArrayList<>();
		TimeSeries timeSeries = createTimeSeriesFromJsonString(jsonString);
		inputList.add(timeSeries);
    	List<TimeSeries> merged = timeSeriesService.mergeTimeseriesData(inputList);
    	assertThat(merged.equals(inputList));
    }
    
    @Test
    @Order(6)
    public void mergeTimeseriesData_twoTimeSeries() {
    	long testValue1 = 56000l; 
    	long testValue2 = 12500l; 
    	ArrayList<TimeSeries> inputList = new ArrayList<>();
    	TimeSeries timeSeries = null;
		int maxIndex = 0;
    	{
	    	timeSeries = createTimeSeriesFromJsonString(jsonString);
	    	maxIndex = timeSeries.getSeries().size()-1;
	    	timeSeries.getSeries().set(0, testValue1);
	    	timeSeries.getSeries().set(maxIndex, testValue2);
	    	inputList.add(timeSeries);
    	}
    	{
	    	timeSeries = createTimeSeriesFromJsonString(jsonString);
	    	timeSeries.setTimestamp(timeSeries.getTimestamp().plusHours(2));
	    	inputList.add(timeSeries);
    	}
    	List<TimeSeries> merged = timeSeriesService.mergeTimeseriesData(inputList);
    	long v1 = merged.get(1).getSeries().get(0);
    	long v2 = merged.get(1).getSeries().get(maxIndex);
    	SoftAssertions assertions = new SoftAssertions();
    	assertions.assertThat(v1).isEqualTo(testValue1);
    	assertions.assertThat(v2).isNotEqualTo(testValue2);
    	assertions.assertAll();
    }
    
    @Test
    @Order(7)
    public void mergeTimeseriesData_threeTimeSeries() {
    	long testValue1 = 56000l; 
    	long testValue2 = 12500l; 
    	long testValue3 = 66050l; 
    	long testValue4 = 42505l; 
    	long testValue5 =  1550l; 
    	long testValue6 = 33100l; 
    	ArrayList<TimeSeries> inputList = new ArrayList<>();
    	TimeSeries timeSeries = null;
		int maxIndex = 0;
    	{
	    	timeSeries = createTimeSeriesFromJsonString(jsonString);
	    	maxIndex = timeSeries.getSeries().size()-1;
	    	timeSeries.getSeries().set(0, testValue1);
	    	timeSeries.getSeries().set(maxIndex, testValue2);
	    	inputList.add(timeSeries);
    	}
    	{
	    	timeSeries = createTimeSeriesFromJsonString(jsonString);
	    	timeSeries.setTimestamp(timeSeries.getTimestamp().plusHours(2));
	    	timeSeries.getSeries().set(1, testValue3);
	    	timeSeries.getSeries().set(maxIndex-1, testValue4);
	    	inputList.add(timeSeries);
    	}
    	{
    		timeSeries = createTimeSeriesFromJsonString(jsonString);
    		timeSeries.setTimestamp(timeSeries.getTimestamp().plusHours(3));
	    	timeSeries.getSeries().set(2, testValue5);
	    	timeSeries.getSeries().set(maxIndex-2, testValue6);
    		inputList.add(timeSeries);
    	}
    	List<TimeSeries> merged = timeSeriesService.mergeTimeseriesData(inputList);
    	SoftAssertions assertions = new SoftAssertions();
    	assertions.assertThat(merged.get(2).getSeries().get(0)).isEqualTo(testValue1);
    	assertions.assertThat(merged.get(2).getSeries().get(maxIndex)).isNotEqualTo(testValue2);
    	assertions.assertThat(merged.get(2).getSeries().get(1)).isNotEqualTo(testValue3);
    	assertions.assertThat(merged.get(2).getSeries().get(maxIndex-1)).isNotEqualTo(testValue4);
    	assertions.assertThat(merged.get(2).getSeries().get(2)).isNotEqualTo(testValue5);
    	assertions.assertThat(merged.get(2).getSeries().get(maxIndex-2)).isEqualTo(testValue6);
    	assertions.assertAll();
    	/*
    	56000,		0,		0, .... ,		0,		 0,	12500
	    	0,	66050,		0, .... ,		0,	42505l,		0
	    -------------------------------------------------
	    56000,		0,		0, .... ,		0,	42505l,		0
	    	0,	 1550,		0, .... ,	33100,		 0,		0
	    -------------------------------------------------
	    56000,		0,		0, .... ,	33100,		 0,		0
	    */ 
   }
    
    
    @Test
    @Order(99)
    public void deleteTestTimeseriesRows() {
		Optional<List<TimeSeries>> finded = timeSeriesRepository.findByDateBetween(
				LocalDate.parse("1999-01-01", TimeSeriesService.dateFormatter), 
				LocalDate.parse("1999-12-31", TimeSeriesService.dateFormatter));
		timeSeriesRepository.deleteAll(finded.get());
    }
    
    /**
     * @param objectMapper
     * @return
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    private TimeSeries addOneTimeSeries(ObjectMapper objectMapper, String dateTimeString )
    		throws JsonProcessingException, JsonMappingException {
    	TimeSeries timeSeries;
    	timeSeries = objectMapper.readValue(jsonString, TimeSeries.class);
    	int newVersion = timeSeriesService.getFreeVersionNumber(timeSeries.getPowerStation(), timeSeries.getDate());
    	timeSeries.setVersion(newVersion);
    	timeSeries.setTimestamp(LocalDateTime.parse(dateTimeString, TimeSeriesService.dateTimeFormatter));
    	timeSeries = timeSeriesRepository.save(timeSeries);
    	return timeSeries;
    }
    

	/**
	 * @return
	 */
	private TimeSeries createTimeSeriesFromJsonString(String inputString) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		TimeSeries timeSeries = null;
		try {
			timeSeries = objectMapper.readValue(jsonString, TimeSeries.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return timeSeries;
	}

    private String jsonString =
        	"""
    		 {
    		    "power-station": "Naper\u0151m\u0171 2021 Kft. Iborfia",
    		    "date": "1999-06-28",
    		    "zone": "Europe/Budapest",
    		    "period": "PT15M",
    		    "timestamp": "1999-06-28 13:29:53",
    		    "series": [
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        3160,
    		        6861,
    		        11336,
    		        16806,
    		        23171,
    		        34751,
    		        57154,
    		        81201,
    		        105757,
    		        131570,
    		        157159,
    		        182121,
    		        207386,
    		        232296,
    		        256428,
    		        279393,
    		        301824,
    		        323420,
    		        344083,
    		        363067,
    		        380667,
    		        397886,
    		        413210,
    		        426305,
    		        437351,
    		        446688,
    		        454164,
    		        460393,
    		        464035,
    		        465306,
    		        466377,
    		        463921,
    		        458288,
    		        451457,
    		        445296,
    		        437518,
    		        428141,
    		        418211,
    		        406459,
    		        393051,
    		        378487,
    		        361539,
    		        343178,
    		        324647,
    		        304919,
    		        284240,
    		        261583,
    		        237907,
    		        213367,
    		        188902,
    		        163630,
    		        137883,
    		        112914,
    		        89161,
    		        65772,
    		        43821,
    		        28602,
    		        22205,
    		        16330,
    		        11027,
    		        6291,
    		        2668,
    		        561,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0,
    		        0
    		    ]
    		}
           """;

}
