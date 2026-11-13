package com.cinema.catalog.adapter.out.persistence;

import com.cinema.catalog.adapter.out.persistence.mapper.SessionPersistenceMapper;
import com.cinema.catalog.adapter.out.persistence.repository.SessionJpaRepository;
import com.cinema.catalog.application.port.out.SessionRepository;
import com.cinema.catalog.domain.exception.SessionOverlapException;
import com.cinema.catalog.domain.model.room.RoomId;
import com.cinema.catalog.domain.model.session.Session;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@Repository
public class SessionPersistenceAdapter implements SessionRepository {
    // SQLState de exclusion_violation; disparado pela constraint sessions_no_overlap (V3)
    private static final String EXCLUSION_VIOLATION = "23P01";

    private final SessionJpaRepository jpaRepository;
    private final SessionPersistenceMapper mapper;

    public SessionPersistenceAdapter(SessionJpaRepository jpaRepository, SessionPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(Session session) {
        try {
            jpaRepository.saveAndFlush(mapper.toEntity(session));
        } catch (DataIntegrityViolationException e) {
            if (e.getMostSpecificCause() instanceof SQLException sql
                    && EXCLUSION_VIOLATION.equals(sql.getSQLState())) {
                throw new SessionOverlapException();
            }
            throw e;
        }
    }

    @Override
    public List<Session> findByRoomIdOverlapping(RoomId roomId, Instant from, Instant to) {
        return jpaRepository.findOverlapping(roomId.value(), from, to)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
