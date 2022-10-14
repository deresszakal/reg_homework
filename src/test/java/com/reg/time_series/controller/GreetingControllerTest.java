package com.reg.time_series.controller;

import com.reg.time_series.entity.Greeting;
import com.reg.time_series.entity.GreetingRepository;
import io.restassured.RestAssured;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.Map;
import java.util.stream.Collectors;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.WebApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class GreetingControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private GreetingRepository greetingRepository;

    private Map<String, String> greetingsByLanguage;

    @BeforeEach
    public void initialiseRestAssuredMockMvcWebApplicationContext() {
        RestAssured.port = port;
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);

        greetingsByLanguage = Map.of(
                "en", "Hello!",
                "hu", "Szia!",
                "jp", "Konichiwa!"
        );
        var greetings = greetingsByLanguage.entrySet().stream()
                .map(e -> createGreeting(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        greetingRepository.saveAll(greetings);
    }

    @AfterEach
    public void clearDatabase() {
        greetingRepository.deleteAll();
    }

    private Greeting createGreeting(String language, String text) {
        var g = new Greeting();
        g.setLanguage(language);
        g.setText(text);
        return g;
    }

    @Test
    public void getGreetings_returnsAllGreetings_whenNoLanguageDefined() {
        RestAssured.when().get("/api/greetings").then()
                .statusCode(HttpStatus.OK.value())
                .body("$", Matchers.hasSize(greetingsByLanguage.size()));
    }

    @Test
    public void getGreetings_returnsGreetingForLanguage_ifLanguageIsSpecified() {
        RestAssured.when().get("/api/greetings?language=hu").then()
                .statusCode(HttpStatus.OK.value())
                .body("text", Matchers.contains(greetingsByLanguage.get("hu")));
    }

    @Test
    public void getGreetings_returnsEmptyCollection_forNonExistentLanguage() {
        RestAssured.when().get("/api/greetings?language=non-existent-language").then()
                .statusCode(HttpStatus.OK.value())
                .body("$", Matchers.empty());
    }

    @Test
    public void putGreeting_createsNewGreeting() {
        var spanishLanguageCode = "es";
        var spanishGreeting = "Hola";
        RestAssured.when().put("/api/greetings?language=" + spanishLanguageCode + "&text=" + spanishGreeting).then()
                .statusCode(HttpStatus.OK.value());

        var savedGreeting = greetingRepository.findByLanguage(spanishLanguageCode);
        assertThat(savedGreeting).isNotEmpty();
        assertThat(savedGreeting.get().getLanguage()).isEqualTo(spanishLanguageCode);
        assertThat(savedGreeting.get().getText()).isEqualTo(spanishGreeting);
    }

    @Test
    public void putGreeting_changesExistingGreeting() {
        var hungarianLanguageCode = "hu";
        var newHungarianGreeting = "Szevasz";
        RestAssured.when().put("/api/greetings?language=" + hungarianLanguageCode + "&text=" + newHungarianGreeting).then()
                .statusCode(HttpStatus.OK.value());

        var savedGreeting = greetingRepository.findByLanguage(hungarianLanguageCode);
        assertThat(savedGreeting).isNotEmpty();
        assertThat(savedGreeting.get().getLanguage()).isEqualTo(hungarianLanguageCode);
        assertThat(savedGreeting.get().getText()).isEqualTo(newHungarianGreeting);
    }

    @Test
    public void deleteGreeting_removesAllGreetings() {
        RestAssured.when().delete("/api/greetings").then().statusCode(HttpStatus.OK.value());

        assertThat(greetingRepository.findAll()).isEmpty();
    }

    @Test
    public void deleteGreeting_removesGreetingByLanguage() {
        var languageToRemove = "en";
        RestAssured.when().delete("/api/greetings?language=" + languageToRemove)
                .then().statusCode(HttpStatus.OK.value());

        assertThat(greetingRepository.findAll()).hasSize(greetingsByLanguage.size() - 1);
        assertThat(greetingRepository.findAll())
                .extracting(Greeting::getLanguage)
                .doesNotContain(languageToRemove);
    }

    @Test
    public void deleteGreeting_doesNothing_ifRemovableLanguageDoesNotExists() {
        RestAssured.when().delete("/api/greetings?language=non-existent-language")
                .then().statusCode(HttpStatus.OK.value());

        assertThat(greetingRepository.findAll()).hasSize(greetingsByLanguage.size());
    }

}