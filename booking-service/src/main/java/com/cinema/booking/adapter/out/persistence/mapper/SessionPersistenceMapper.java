package com.cinema.booking.adapter.out.persistence.mapper;

import com.cinema.booking.adapter.out.persistence.entity.SessionJpaEntity;
import com.cinema.booking.domain.model.session.SeatCode;
import com.cinema.booking.domain.model.session.Session;
import com.cinema.booking.domain.model.session.SessionId;
import com.cinema.booking.domain.model.shared.Money;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SessionPersistenceMapper {

    public SessionJpaEntity toEntity(Session session) {
        List<String> seats = session.getSeats().stream()
                .map(SeatCode::value)
                .toList();
        return new SessionJpaEntity(
                session.getId().value(),
                session.getStartsAt(),
                session.getPrice().amount(),
                seats);
    }

    public Session toDomain(SessionJpaEntity entity) {
        List<SeatCode> seats = entity.getSeats().stream()
                .map(SeatCode::new).toList();

        return Session.restore(
                new SessionId(entity.getId()),
                entity.getStartsAt(),
                new Money(entity.getPrice()),
                seats
        );
    }
}
