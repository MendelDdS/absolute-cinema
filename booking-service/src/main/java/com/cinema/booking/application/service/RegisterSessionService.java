package com.cinema.booking.application.service;

import com.cinema.booking.application.port.in.RegisterSessionCommand;
import com.cinema.booking.application.port.in.RegisterSessionUseCase;
import com.cinema.booking.application.port.out.SessionRepository;
import com.cinema.booking.domain.model.session.SeatCode;
import com.cinema.booking.domain.model.session.Session;
import com.cinema.booking.domain.model.session.SessionId;
import com.cinema.booking.domain.model.shared.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegisterSessionService implements RegisterSessionUseCase {

    private final SessionRepository sessionRepository;

    public RegisterSessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    @Transactional
    public void register(RegisterSessionCommand command) {
        List<SeatCode> seatCodes = command.seatCodes().stream().map(SeatCode::new).toList();
        Session session = Session.register(new SessionId(command.sessionId()), command.startsAt(), new Money(command.price()), seatCodes);
        this.sessionRepository.save(session);
    }
}
