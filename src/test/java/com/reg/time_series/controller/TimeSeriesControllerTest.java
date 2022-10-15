/**
 * 
 */
package com.reg.time_series.controller;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.io.File;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.WebApplicationContext;

import io.restassured.RestAssured;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

/**
 * @author phars
 *
 */

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class TimeSeriesControllerTest {
	
    @LocalServerPort
    int port;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void initAssuredMockMvcWebApplicationContext() {
        RestAssured.port = port;
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
    }
    
    @Test
	public void upload_jsonFile() {
		RestAssured
		  .given()
		  	.contentType(MediaType.APPLICATION_JSON_VALUE)
	        .body(new File("C:\\workspace_peter.javaeclipse\\reg_homework\\sample_data\\ps_83_20210628_032953.json"))
		  .when().put("/timeseries/upload").then()
		  .statusCode(HttpStatus.OK.value());
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
	
}
