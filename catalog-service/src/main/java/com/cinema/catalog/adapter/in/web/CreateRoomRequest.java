package com.cinema.catalog.adapter.in.web;

import com.cinema.catalog.application.port.in.CreateRoomCommand;

public record CreateRoomRequest(String name, int rows, int seatsPerRow) {
    CreateRoomCommand toCommand() {
        return new CreateRoomCommand(name, rows, seatsPerRow);
    }
}
