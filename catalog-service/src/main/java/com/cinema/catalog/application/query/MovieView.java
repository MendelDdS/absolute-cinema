package com.cinema.catalog.application.query;

import java.util.UUID;

public record MovieView(UUID id, String title, int durationMinutes) { }
