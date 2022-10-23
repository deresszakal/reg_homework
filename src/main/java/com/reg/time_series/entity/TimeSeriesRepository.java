package com.reg.time_series.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
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
	
	@Query(nativeQuery = true, value = "SELECT * FROM time_series GROUP by powerstation")
	Optional<List<TimeSeries>> findGroupByPowerStation();
	
	@Query(nativeQuery = true, value = "SELECT * FROM time_series where date != :date GROUP BY powerstation")
	Optional<List<TimeSeries>>  findPowerStationByDateGroupByPowerStation(@Param("date") String date);
	
	@Query(nativeQuery = true, value = "SELECT * FROM time_series where powerstation = :powerstation GROUP BY date")
	Optional<List<TimeSeries>>  findPowerStationByPowerStationGroupByDate(@Param("powerstation") String powerstation);
	
	Optional<List<TimeSeries>> findByPowerStation(String powerstation);

	Optional<List<TimeSeries>> findByPowerStationAndDate(String powerstation, LocalDate date);
	
//	Long deleteByDateBetween(LocalDate from, LocalDate to);

}
