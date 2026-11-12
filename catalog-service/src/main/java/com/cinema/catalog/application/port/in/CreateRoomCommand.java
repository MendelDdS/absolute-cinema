package com.cinema.catalog.application.port.in;

public record CreateRoomCommand(String name, int rows, int seatsPerRow) {}