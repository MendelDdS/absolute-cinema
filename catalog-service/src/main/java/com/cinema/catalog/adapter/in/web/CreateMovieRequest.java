package com.cinema.catalog.adapter.in.web;

import com.cinema.catalog.application.port.in.CreateMovieCommand;

public record CreateMovieRequest(String title, int durationMinutes) {
    CreateMovieCommand toCommand() {
        return new CreateMovieCommand(title, durationMinutes);
    }
}
