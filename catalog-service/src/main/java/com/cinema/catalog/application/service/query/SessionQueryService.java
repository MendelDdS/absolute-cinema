package com.cinema.catalog.application.service.query;

import com.cinema.catalog.application.exception.SessionNotFoundException;
import com.cinema.catalog.application.port.in.query.SessionFilter;
import com.cinema.catalog.application.port.in.query.SessionQueryUseCase;
import com.cinema.catalog.application.port.out.query.SessionQueryRepository;
import com.cinema.catalog.application.query.SessionView;
import com.cinema.catalog.domain.model.session.SessionId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SessionQueryService implements SessionQueryUseCase {

    private final SessionQueryRepository sessionRepository;
    private final ZoneId cinemaZone;

    public SessionQueryService(SessionQueryRepository sessionRepository, ZoneId cinemaZone) {
        this.sessionRepository = sessionRepository;
        this.cinemaZone = cinemaZone;
    }

    @Override
    public List<SessionView> list(SessionFilter filter) {
        Instant from = filter.date().atStartOfDay(cinemaZone).toInstant();
        Instant to = filter.date().plusDays(1).atStartOfDay(cinemaZone).toInstant();
        return sessionRepository.findStartingBetween(filter.movieId(), from, to);
    }

    @Override
    public SessionView get(SessionId id) {
        return sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(id));
    }
}
