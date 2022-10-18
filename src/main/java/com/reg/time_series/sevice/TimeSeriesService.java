package com.reg.time_series.sevice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	
	@Autowired
	ApplicationService applicationService;
	
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public int getFreeVersionNumber(String powerStation, LocalDate date ) {
		Optional<TimeSeries> result = timeSeriesRepository.findFirstByPowerStationAndDateOrderByVersionDesc(powerStation, date);
		int oldVersion = 0;
		if (result.isPresent()) {
			oldVersion = result.get().getVersion();
		}
		return ++oldVersion;
	}

	public boolean isTimeSeriesNewer(TimeSeries timeSeries) {
		boolean newer = false;
		Optional<TimeSeries> result = timeSeriesRepository.findFirstByPowerStationAndDateOrderByTimestampDesc(timeSeries.getPowerStation(), timeSeries.getDate());
		if (result.isEmpty() || 
			timeSeries.getTimestamp().isAfter(result.get().getTimestamp()) ||
			timeSeries.getTimestamp().isEqual(result.get().getTimestamp())
			) {
			newer = true;
		}
		return newer;
	}
	
	public LocalDateTime calculateSafetyWindowEnd(LocalDateTime start) {
		int safetyWindowSize = applicationService.getSafetyWindowSizeInMinutes();
		int timeSlice = applicationService.getTimeSlice();
		LocalDateTime end = start.plusMinutes(safetyWindowSize);
		LocalDateTime nextSliced = end.truncatedTo(ChronoUnit.HOURS).plusMinutes(timeSlice * ((end.getMinute() / timeSlice)+1));
		boolean sameDay = dateFormatter.format(start).equals(dateFormatter.format(nextSliced));
		if (!sameDay) {
			nextSliced = nextSliced.minusDays(1).truncatedTo(ChronoUnit.DAYS).plusMinutes(24*60);
		}
		return nextSliced;
	}
	
	public static void main(String[] args) {
	    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime ldt1 = LocalDateTime.parse("1999-06-28 13:29:53", dateTimeFormatter);
		LocalDateTime ldt2 = LocalDateTime.parse("1999-06-28 13:29:53", dateTimeFormatter);
		LocalDateTime ldt3 = LocalDateTime.parse("1999-06-28 13:29:52", dateTimeFormatter);
		
		System.out.println( ldt1.toString() + "; "+ ldt2.toString() + "; after;  " + ldt1.isAfter(ldt2));
		System.out.println( ldt1.toString() + "; "+ ldt2.toString() + "; before; " + ldt1.isBefore(ldt2));
		System.out.println( ldt1.toString() + "; "+ ldt2.toString() + "; equal;  " + ldt1.isEqual(ldt2));
		System.out.println("");
		System.out.println( ldt1.toString() + "; "+ ldt3.toString() + "; after;  " + ldt1.isAfter(ldt3));
		System.out.println( ldt1.toString() + "; "+ ldt3.toString() + "; before; " + ldt1.isBefore(ldt3));
		System.out.println( ldt1.toString() + "; "+ ldt3.toString() + "; equal;  " + ldt1.isEqual(ldt3));
		System.out.println("");
		System.out.println( ldt3.toString() + "; "+ ldt1.toString() + "; after;  " + ldt3.isAfter(ldt1));
		System.out.println( ldt3.toString() + "; "+ ldt1.toString() + "; before; " + ldt3.isBefore(ldt1));
		System.out.println( ldt3.toString() + "; "+ ldt1.toString() + "; equal;  " + ldt3.isEqual(ldt1));
		System.out.println("");
	}
	
}
