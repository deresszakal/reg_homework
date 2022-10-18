/**
 * 
 */
package com.reg.time_series.controller;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.reg.time_series.entity.TimeSeries;
import com.reg.time_series.entity.TimeSeriesRepository;

import io.restassured.RestAssured;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import static org.hamcrest.CoreMatchers.containsString;

/**
 * @author phars
 *
 */

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TimeSeriesControllerTest {
	
    @LocalServerPort
    int port;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TimeSeriesRepository timeSeriesRepository;
    
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @BeforeEach
    public void initAssuredMockMvcWebApplicationContext() {
        RestAssured.port = port;
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
    }
    
    @Test
    @Order(0)
    public void clean() {
		Optional<List<TimeSeries>> finded = timeSeriesRepository.findByDateBetween(LocalDate.parse("1999-01-01", dateFormatter), LocalDate.parse("1999-12-31", dateFormatter));
		timeSeriesRepository.deleteAll(finded.get());
    }
    
    @Test
    @Order(1)
	public void upload_jsonFile() {
		RestAssured
		  .given()
		  	.contentType(MediaType.APPLICATION_JSON_VALUE)
	        .body(jsonString)
		  .when().put("/timeseries/upload").then()
		  	.statusCode(HttpStatus.OK.value());
	}

    @Test
    @Order(2)
    public void upload_emptyBody() {
    	RestAssured
    	.given()
    		.contentType(MediaType.APPLICATION_JSON_VALUE)
    	.when().put("/timeseries/upload")
    	.then().assertThat()
    		.statusCode(HttpStatus.BAD_REQUEST.value())
    		.body(containsString("Bad Request"));
    }
    
    @Test
    @Order(3)
    public void upload_uncorrectJsonDateFormat() {
    	jsonString = jsonString.replaceAll("(?<=[\\d\\.])-(?=[\\d\\.])", "\\$");
    	RestAssured
    	.given()
    		.contentType(MediaType.APPLICATION_JSON_VALUE)
    		.body(jsonString)
    	.when().put("/timeseries/upload")
    	.then().assertThat()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.body(containsString(TimeSeriesController.MSG_DATA_NOT_PARSERABLE));
    }
    
    
    @Test
    @Order(4)
    public void upload_uncorrectJsonFieldName() {
    	String jsonStringChanged = jsonString.replace('-', '$');
    	RestAssured
    	.given()
    		.contentType(MediaType.APPLICATION_JSON_VALUE)
    		.body(jsonStringChanged)
    	.when().put("/timeseries/upload")
    	.then().assertThat()
    		.statusCode(HttpStatus.BAD_REQUEST.value())
    		.body(containsString(TimeSeriesController.MSG_DATA_NOT_PARSERABLE));
    }
    
    @Test
    @Order(5)
    public void upload_sameFile() {
    	RestAssured
    	.given()
    		.contentType(MediaType.APPLICATION_JSON_VALUE)
    		.body(jsonString)
    	.when().put("/timeseries/upload")
    	.then().assertThat()
    		.statusCode(HttpStatus.BAD_REQUEST.value())
    		.body(containsString(TimeSeriesController.MSG_UNIQUEPOWERSTATIONDATETIMESTAMP_CV));
    }
    
    @Test
    @Order(6)
    public void upload_OlderFile() {
    	String jsonStringChanged = jsonString.replace("1999-06-28 13:29:53", "1999-06-28 13:29:52");
    	RestAssured
    	.given()
    		.contentType(MediaType.APPLICATION_JSON_VALUE)
    		.body(jsonStringChanged)
    	.when().put("/timeseries/upload")
    	.then().assertThat()
    		.statusCode(HttpStatus.BAD_REQUEST.value())
    		.body(containsString(TimeSeriesController.MSG_DATA_NOT_NEW));
    }
    
    @Test
    @Order(7)
    public void upload_versionCollision() {
    	String jsonStringChanged = jsonString.replace("\"period\": \"PT15M\",", "\"period\": \"PT15M\",\n\t\"version\": \"1\",");
    	jsonStringChanged = jsonStringChanged.replace("1999-06-28 13:29:53", "1999-06-28 13:30:53");
    	RestAssured
    	.given()
    	.contentType(MediaType.APPLICATION_JSON_VALUE)
    	.body(jsonStringChanged)
    	.when().put("/timeseries/upload")
    	.then().assertThat()
    	.statusCode(HttpStatus.BAD_REQUEST.value())
    	.body(containsString(TimeSeriesController.MSG_UNIQUEPOWERSTATIONDATEVERSION_CV));
    }
    
    
	//@Test
	public void upload_jsonFileMultipart() {
		RestAssured
		  .given()
	         .param("timestamp", new Date().getTime())
	         .multiPart("file", new File("C:\\workspace_peter.javaeclipse\\reg_homework\\sample_data\\ps_83_20210628_032953.json"))
		  .when().put("/timeseries/upload").then()
		  .statusCode(HttpStatus.OK.value());
	}
	
    @Test
    @Order(99)
    public void deleteTestTimeseriesRows() {
		Optional<List<TimeSeries>> finded = timeSeriesRepository.findByDateBetween(LocalDate.parse("1999-01-01", dateFormatter), LocalDate.parse("1999-12-31", dateFormatter));
		timeSeriesRepository.deleteAll(finded.get());
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
