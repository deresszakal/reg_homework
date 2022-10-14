package com.reg.time_series.controller;

import com.reg.time_series.entity.Greeting;
import com.reg.time_series.entity.GreetingRepository;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class GreetingController {

    private final GreetingRepository greetingRepository;

    @Autowired
    public GreetingController(GreetingRepository greetingRepository) {
        this.greetingRepository = greetingRepository;
    }

    @GetMapping("/greetings")
    public List<Greeting> greetings(@RequestParam(required = false) String language) {
        if (language == null || language.isEmpty()) {
            return greetingRepository.findAll();
        }
        return greetingRepository.findByLanguage(language)
                .map(List::of).orElse(Collections.emptyList());
    }

    @PutMapping("/greetings")
    public ResponseEntity<Void> addGreeting(@RequestParam String language, @RequestParam String text) {
        var greeting = greetingRepository.findByLanguage(language).orElse(new Greeting());
        greeting.setLanguage(language);
        greeting.setText(text);
        greetingRepository.save(greeting);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/greetings")
    public ResponseEntity<Void> removeGreeting(@RequestParam(required = false) String language) {
        if (language == null || language.isEmpty()) {
            greetingRepository.deleteAll();
        } else {
            greetingRepository.findByLanguage(language).ifPresent(greetingRepository::delete);
        }
        return ResponseEntity.ok().build();
    }
}
