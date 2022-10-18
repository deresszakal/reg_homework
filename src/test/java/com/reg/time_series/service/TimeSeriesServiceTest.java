package com.reg.time_series.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.reg.time_series.entity.TimeSeries;
import com.reg.time_series.entity.TimeSeriesRepository;
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
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		TimeSeries timeSeries = null;
		try {
			timeSeries = objectMapper.readValue(jsonString, TimeSeries.class);
			int newVersion = timeSeriesService.getFreeVersionNumber(timeSeries.getPowerStation(), timeSeries.getDate());
			timeSeries.setVersion(newVersion);
			timeSeries = timeSeriesRepository.save(timeSeries);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
