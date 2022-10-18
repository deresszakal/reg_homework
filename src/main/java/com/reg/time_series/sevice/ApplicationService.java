package com.reg.time_series.sevice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author phars
 *
 */
@Service
public class ApplicationService {

	@Value("${application.safety_window.size}")
	private String safetyWindowSize;
	
	@Value("${application.series.time_slice}")
	private int timeSlice;

    public int getSafetyWindowSizeInMinutes() {
    	return Integer.parseInt(safetyWindowSize);
    }
    
    public int getTimeSlice() {
    	return timeSlice;
    }

}
