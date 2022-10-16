package com.reg.time_series.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author phars
 *
 */

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TimeSeries {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    
    @JsonProperty("power-station")
	String powerStation;

    LocalDate date;
	
    String zone;
	
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp;
	
    String period;
	
    ArrayList<Long> series;
    
    int version;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getPowerStation());
		sb.append("; ");
		sb.append(getDate());
		sb.append("; ");
		sb.append(getVersion());
		return sb.toString();
	}

    
    
}
