package com.reg.time_series.sevice;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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
		int timeSlice = applicationService.getTimeSlice();
		int safetyWindowSizeInMinutes = applicationService.getSafetyWindowSizeInMinutes();
		LocalDateTime end = start.plusMinutes(safetyWindowSizeInMinutes);
		LocalDateTime nextSlice = null;
		if (isExactSlice(end)) {
			nextSlice = end;
		}
		else {
			nextSlice = end.truncatedTo(ChronoUnit.HOURS).plusMinutes(timeSlice * ((end.getMinute() / timeSlice)+1));
		}
		boolean sameDay = dateFormatter.format(start).equals(dateFormatter.format(nextSlice));
		if (!sameDay) {
			nextSlice = nextSlice.minusDays(1).truncatedTo(ChronoUnit.DAYS).plusMinutes(24*60);
		}
		return nextSlice;
	}
	
	public boolean isExactSlice(LocalDateTime dateTime) {
		boolean result = false;
		int timeSlice = applicationService.getTimeSlice();
		long minutesFromMidnight = Duration.between(dateTime, dateTime.truncatedTo(ChronoUnit.DAYS)).toMinutes();
		if (minutesFromMidnight % timeSlice == 0) {
			result = true;
		}
		return result;
	}
	
	public int convertDatetimeToIndex(LocalDateTime dateTime) {
		int result = -1;
		int timeSlice = applicationService.getTimeSlice();
		LocalDateTime nextSlice = dateTime;
		if (!isExactSlice(dateTime)) {
			nextSlice = dateTime.truncatedTo(ChronoUnit.HOURS).plusMinutes(timeSlice * ((dateTime.getMinute() / timeSlice)+1));
		}
		Duration duration = Duration.between(dateTime.truncatedTo(ChronoUnit.DAYS), nextSlice);
		int minutes = ((Long)duration.toMinutes()).intValue();
		result = minutes / timeSlice;
		return result;
	}
	
	public LocalDateTime convertIndexToDatetime(int index, LocalDateTime dateTime) {
		LocalDateTime result = null;
		int timeSlice = applicationService.getTimeSlice();
		result = dateTime.truncatedTo(ChronoUnit.DAYS).plusMinutes(timeSlice * index);
		return result;
	}
	
	public List<TimeSeries> mergeTimeseriesData(List<TimeSeries> inputTimeSeries) {
//		ArrayList<TimeSeries> result = new ArrayList<>(List.copyOf(inputTimeSeries)); Az elemek csak bemásolódtak :(
		ArrayList<TimeSeries> result = (ArrayList<TimeSeries>)inputTimeSeries;
		for (int i=1; i<=result.size()-1; i++) {
			ArrayList<Long> beforeSeries = result.get(i-1).getSeries();
			ArrayList<Long> aktSeries = result.get(i).getSeries();
			LocalDateTime safetyWindowEnd = calculateSafetyWindowEnd(result.get(i).getTimestamp());
			int end = convertDatetimeToIndex(safetyWindowEnd);
			for (int j=0; j<=end; j++) {
				aktSeries.set(j, beforeSeries.get(j));
			}
		}
		return result;
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
