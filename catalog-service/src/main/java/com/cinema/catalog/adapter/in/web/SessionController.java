package com.cinema.catalog.adapter.in.web;

import com.cinema.catalog.application.port.in.ScheduleSessionUseCase;
import com.cinema.catalog.application.port.in.query.SessionFilter;
import com.cinema.catalog.application.port.in.query.SessionQueryUseCase;
import com.cinema.catalog.application.query.SessionView;
import com.cinema.catalog.domain.model.movie.MovieId;
import com.cinema.catalog.domain.model.session.SessionId;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sessions")
class SessionController {

    private final ScheduleSessionUseCase scheduleSession;
    private final SessionQueryUseCase querySession;

    public SessionController(ScheduleSessionUseCase scheduleSession, SessionQueryUseCase querySession) {
        this.scheduleSession = scheduleSession;
        this.querySession = querySession;
    }

    @PostMapping
    ResponseEntity<Void> schedule(@Valid @RequestBody ScheduleSessionRequest request) {
        SessionId sessionId = scheduleSession.schedule(request.toCommand());
        return ResponseEntity.created(URI.create("/sessions/" + sessionId.value())).build();
    }

    @GetMapping
    List<SessionView> list(@RequestParam(required = false) UUID movieId,
                                           @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date ) {
        MovieId movie = movieId != null ? new MovieId(movieId) : null;
        return querySession.list(new SessionFilter(movie, date));
    }

    @GetMapping("/{id}")
    SessionView findSession(@PathVariable UUID id) {
        return querySession.get(new SessionId(id));
    }
}
