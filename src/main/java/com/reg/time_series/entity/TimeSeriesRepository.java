package com.reg.time_series.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author phars
 *
 */
@Repository
public interface TimeSeriesRepository extends CrudRepository<TimeSeries, Long> {
	
	Optional<TimeSeries> findFirstByPowerStationAndDateOrderByVersionDesc(String powerstation, LocalDate date);

	Optional<TimeSeries> findFirstByPowerStationAndDateOrderByTimestampDesc(String powerstation, LocalDate date);
	
	Optional<List<TimeSeries>> findByDateBetween(LocalDate from, LocalDate to);

//	Long deleteByDateBetween(LocalDate from, LocalDate to);

}
