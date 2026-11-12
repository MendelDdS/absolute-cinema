package com.cinema.catalog.application.exception;

import com.cinema.catalog.domain.model.session.SessionId;

public class SessionNotFoundException extends NotFoundException {
    public SessionNotFoundException(SessionId id) {
        super("Sessão não encontrada: " + id.value());
    }
}
