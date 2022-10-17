package com.reg.time_series.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.reg.time_series.sevice.SeriesConverter;

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
@Table(uniqueConstraints = {
		   @UniqueConstraint(name = "UniquePowerstationDateTimestamp", columnNames = {"powerstation", "date", "timestamp"}),
		   @UniqueConstraint(name = "UniquePowerstationDateVerzion", columnNames = {"powerstation", "date", "version"})
	   })
public class TimeSeries {
	
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;
    
    @JsonProperty("power-station")
    @Column(name = "powerstation")
	String powerStation;

    LocalDate date;
	
    String zone;
	
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp;
	
    String period;
	
    @Column(length = 2000)
    @Convert(converter = SeriesConverter.class)
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

//	@PrePersist
//	public void onPrePersist() {
//		 
//
////		 Optional<TimeSeries> result = timeSeriesRepository.findFirsByPowerstationAndDateOrderByVersion(getPowerStation(), getDate().toString());
////		Optional<TimeSeries> r1 = timeSeriesRepository.findByPowerStation(getPowerStation());
////		Optional<TimeSeries> r2 = timeSeriesRepository.findByDate(getDate().toString());
////		Optional<TimeSeries> r2 = timeSeriesRepository.findByZone(getZone());
////		Optional<TimeSeries> r2 = timeSeriesRepository.findOne();
//		int i = TimeSeriesService.getInstance().getNextVersion(powerStation, date );
//		 
//		 System.out.println("TimeSeries.onPrePersist()" );
//	 }
    
}
