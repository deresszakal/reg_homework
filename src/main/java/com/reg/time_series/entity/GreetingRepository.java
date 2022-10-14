package com.reg.time_series.entity;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface GreetingRepository extends CrudRepository<Greeting, Long> {

    @Override
    List<Greeting> findAll();

    Optional<Greeting> findByLanguage(String language);
}
