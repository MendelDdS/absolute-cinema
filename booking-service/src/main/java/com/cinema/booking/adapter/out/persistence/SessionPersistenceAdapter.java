package com.cinema.booking.adapter.out.persistence;

import com.cinema.booking.adapter.out.persistence.mapper.SessionPersistenceMapper;
import com.cinema.booking.adapter.out.persistence.repository.SessionJpaRepository;
import com.cinema.booking.application.port.out.SessionRepository;
import com.cinema.booking.domain.model.session.Session;
import com.cinema.booking.domain.model.session.SessionId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SessionPersistenceAdapter implements SessionRepository {
    private final SessionJpaRepository jpaRepository;
    private final SessionPersistenceMapper mapper;

    public SessionPersistenceAdapter(SessionJpaRepository jpaRepository, SessionPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(Session session) {
        jpaRepository.save(mapper.toEntity(session));
    }

    @Override
    public Optional<Session> findById(SessionId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }
}
