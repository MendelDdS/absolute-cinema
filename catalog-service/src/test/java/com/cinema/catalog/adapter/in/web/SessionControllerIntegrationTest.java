package com.cinema.catalog.adapter.in.web;

import com.cinema.catalog.IntegrationTest;
import com.cinema.catalog.application.port.out.MovieRepository;
import com.cinema.catalog.application.port.out.RoomRepository;
import com.cinema.catalog.domain.model.movie.Movie;
import com.cinema.catalog.domain.model.room.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SessionControllerIntegrationTest extends IntegrationTest {

    private static final String STARTS_AT = "2030-01-10T19:00:00-03:00";

    @Autowired
    private MockMvcTester mvc;
    @Autowired
    private MovieRepository movies;
    @Autowired
    private RoomRepository rooms;

    private Movie movie;
    private Room room;

    @BeforeEach
    void setUp() {
        movie = Movie.create("Duna", Duration.ofMinutes(120));
        room = Room.withGrid("Sala 1", 2, 3);
        movies.save(movie);
        rooms.save(room);
    }

    private String body(UUID movieId, UUID roomId, String startsAt) {
        return """
                {"movieId": "%s", "roomId": "%s", "startsAt": "%s", "price": 30.00}
                """.formatted(movieId, roomId, startsAt);
    }

    private MvcTestResult postSession(String json) {
        return mvc.post().uri("/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange();
    }

    private MvcTestResult getSession(String date) {
        return mvc.get().uri("/sessions")
                .param("date", date)
                .exchange();
    }

    @Test
    @DisplayName("POST deve responder 201 com Location")
    void createsSession() {
        assertThat(postSession(body(movie.getId().value(), room.getId().value(), STARTS_AT)))
                .hasStatus(HttpStatus.CREATED)
                .headers().containsHeader("Location");
    }

    @Test
    @DisplayName("POST sem movieId deve responder 400 em Problem Details")
    void rejectsMissingField() {
        String json = """
                {"roomId": "%s", "startsAt": "%s", "price": 30.00}
                """.formatted(room.getId().value(), STARTS_AT);

        assertThat(postSession(json))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.detail").isEqualTo("Invalid request content.");
    }

    @Test
    @DisplayName("POST com data sobrepondo a data de outra sessão deve responder CONFLICT")
    void rejectsOverlappingSession() {
        String json = """
                {"roomId": "%s", "movieId": "%s", "startsAt": "%s", "price": 30.00}
                """.formatted(room.getId().value(), movie.getId().value(), STARTS_AT);

        postSession(json);

        assertThat(postSession(json))
                .hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("POST com filme inexistente deve responder 404 NOT_FOUND")
    void returnsNotFoundForUnknownMovie() {
        String json = """
                {"roomId": "%s", "movieId": "%s", "startsAt": "%s", "price": 30.00}
                """.formatted(room.getId().value(), UUID.randomUUID(), STARTS_AT);

        assertThat(postSession(json))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET de sessões sem data deve responder 400 BAD_REQUEST")
    void requiresDateWhenListing() {
        assertThat(getSession(null))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }
}