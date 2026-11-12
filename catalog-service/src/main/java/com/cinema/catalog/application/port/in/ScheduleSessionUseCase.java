package com.cinema.catalog.application.port.in;

import com.cinema.catalog.domain.model.session.SessionId;

public interface ScheduleSessionUseCase {
    SessionId schedule(ScheduleSessionCommand command);
}
