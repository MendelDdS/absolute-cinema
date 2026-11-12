package com.cinema.catalog.application.query;

import java.util.UUID;

public record RoomView(UUID id, String title, int capacity) { }
