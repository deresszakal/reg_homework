package com.reg.time_series.entity;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author phars
 *
 */
@Repository
public interface TimeSeriesRepository extends CrudRepository<TimeSeries, Long> {

}
