package com.reg.time_series.sevice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import com.reg.time_series.entity.TimeSeries;
import com.reg.time_series.entity.TimeSeriesRepository;

/**
 * @author phars
 *
 */
@Service
public class TimeSeriesService {
	
	@Autowired
	private TimeSeriesRepository timeSeriesRepository;
	
	public int getFreeVersionNumber(String powerStation, LocalDate date ) {
		Optional<TimeSeries> result = timeSeriesRepository.findFirstByPowerStationAndDateOrderByVersionDesc(powerStation, date);
		int oldVersion = 0;
		if (result.isPresent()) {
			oldVersion = result.get().getVersion();
		}
		return ++oldVersion;
	}

	public boolean isTimeSeriesNewer(TimeSeries timeSeries) {
		boolean newer = true;
		Optional<TimeSeries> result = timeSeriesRepository.findFirstByPowerStationAndDateOrderByTimestampDesc(timeSeries.getPowerStation(), timeSeries.getDate());
//		if (result.isEmpty() || timeSeries.getTimestamp().isAfter(result.get().getTimestamp())) {
		if (result.isEmpty() || result.get().getTimestamp().isAfter(timeSeries.getTimestamp())) {
			newer = false;
		}
		return newer;
	}
	
	public static void main(String[] args) {
	    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime ldt1 = LocalDateTime.parse("1999-06-28 13:29:53", dateTimeFormatter);
		LocalDateTime ldt2 = LocalDateTime.parse("1999-06-28 13:29:53", dateTimeFormatter);
		LocalDateTime ldt3 = LocalDateTime.parse("1999-06-28 13:29:52", dateTimeFormatter);
		
		System.out.println( ldt1.toString() + "; "+ ldt2.toString() + "; " + ldt1.isAfter(ldt2));
		System.out.println( ldt1.toString() + "; "+ ldt2.toString() + "; " + ldt1.isBefore(ldt2));
		System.out.println( ldt1.toString() + "; "+ ldt2.toString() + "; " + ldt1.isEqual(ldt2));
		System.out.println( ldt1.toString() + "; "+ ldt3.toString() + "; " + ldt1.isAfter(ldt3));
		System.out.println( ldt1.toString() + "; "+ ldt3.toString() + "; " + ldt1.isBefore(ldt3));
		System.out.println( ldt1.toString() + "; "+ ldt3.toString() + "; " + ldt1.isEqual(ldt3));
	}
	
}
