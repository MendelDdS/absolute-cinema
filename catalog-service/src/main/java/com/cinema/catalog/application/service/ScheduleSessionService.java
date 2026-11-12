package com.cinema.catalog.application.service;

import com.cinema.catalog.application.exception.MovieNotFoundException;
import com.cinema.catalog.application.exception.RoomNotFoundException;
import com.cinema.catalog.application.port.in.ScheduleSessionCommand;
import com.cinema.catalog.application.port.in.ScheduleSessionUseCase;
import com.cinema.catalog.application.port.out.MovieRepository;
import com.cinema.catalog.application.port.out.RoomRepository;
import com.cinema.catalog.application.port.out.SessionEventPublisher;
import com.cinema.catalog.application.port.out.SessionRepository;
import com.cinema.catalog.domain.model.movie.Movie;
import com.cinema.catalog.domain.model.room.Room;
import com.cinema.catalog.domain.model.session.Session;
import com.cinema.catalog.domain.model.session.SessionId;
import com.cinema.catalog.domain.model.session.SessionScheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
public class ScheduleSessionService implements ScheduleSessionUseCase {

    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final SessionRepository sessionRepository;
    private final SessionEventPublisher eventPublisher;
    private final Clock clock;

    public ScheduleSessionService(MovieRepository movieRepository, RoomRepository roomRepository, SessionRepository sessionRepository, SessionEventPublisher eventPublisher, Clock clock) {
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.sessionRepository = sessionRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public SessionId schedule(ScheduleSessionCommand command) {
        Movie movie = movieRepository.findById(command.movieId())
                .orElseThrow(() -> new MovieNotFoundException(command.movieId()));
        Room room = roomRepository.findById(command.roomId())
                .orElseThrow(() -> new RoomNotFoundException(command.roomId()));

        Session session = Session.schedule(movie, room, command.startsAt(), command.price(), clock);

        List<Session> sameRoom = sessionRepository.findByRoomIdOverlapping(
                room.getId(), session.getStartsAt(), session.getEndsAt());
        session.ensureNoOverlapWith(sameRoom);

        sessionRepository.save(session);
        eventPublisher.publish(SessionScheduled.of(session, room, clock.instant()));

        return session.getId();
    }
}
