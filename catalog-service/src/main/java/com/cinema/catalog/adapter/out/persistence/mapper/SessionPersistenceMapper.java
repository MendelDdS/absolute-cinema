package com.cinema.catalog.adapter.out.persistence.mapper;

import com.cinema.catalog.adapter.out.persistence.entity.SessionJpaEntity;
import com.cinema.catalog.domain.model.movie.MovieId;
import com.cinema.catalog.domain.model.room.RoomId;
import com.cinema.catalog.domain.model.session.Session;
import com.cinema.catalog.domain.model.session.SessionId;
import com.cinema.catalog.domain.model.shared.Money;
import org.springframework.stereotype.Component;

@Component
public class SessionPersistenceMapper {

    public SessionJpaEntity toEntity(Session session) {
        return new SessionJpaEntity(
                session.getId().value(),
                session.getMovieId().value(),
                session.getRoomId().value(),
                session.getStartsAt(),
                session.getEndsAt(),
                session.getPrice().amount());        // Money → BigDecimal
    }

    public Session toDomain(SessionJpaEntity entity) {
        return Session.restore(
                new SessionId(entity.getId()),
                new MovieId(entity.getMovieId()),
                new RoomId(entity.getRoomId()),
                entity.getStartsAt(),
                entity.getEndsAt(),
                new Money(entity.getPrice()));       // BigDecimal → Money
    }
}