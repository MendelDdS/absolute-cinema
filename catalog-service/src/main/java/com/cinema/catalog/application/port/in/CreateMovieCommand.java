package com.cinema.catalog.application.port.in;

public record CreateMovieCommand(String title, int durationMinutes) {}