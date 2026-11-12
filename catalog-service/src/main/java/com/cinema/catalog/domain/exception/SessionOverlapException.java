package com.cinema.catalog.domain.exception;

public class SessionOverlapException extends DomainException {
    public SessionOverlapException() {
        super("Já existe uma sessão nesta sala no horário informado");
    }
}
